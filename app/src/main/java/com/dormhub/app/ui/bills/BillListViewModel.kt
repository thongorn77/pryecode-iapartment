/*
 * BillListViewModel.kt — ViewModel สำหรับรายการบิลประจำเดือน
 * ============================================================
 * แสดงบิลทั้งหมดในเดือนที่เลือก
 * generateBills() สร้างบิล DRAFT จากข้อมูลมิเตอร์ที่จดแล้ว
 */
package com.dormhub.app.ui.bills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.dao.MeterReadingDao
import com.dormhub.app.data.entity.*
import com.dormhub.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class BillListUiState(
    val yearMonth: Int = 0,
    val bills: List<Bill> = emptyList(),
    val isLoading: Boolean = true,
    val isGenerating: Boolean = false,
    val generateError: String? = null,
)

@HiltViewModel
class BillListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val billRepo: BillRepository,
    private val roomRepo: RoomRepository,
    private val tenantRepo: TenantRepository,
    private val rateRepo: RateRepository,
    private val meterReadingDao: MeterReadingDao,
) : ViewModel() {

    val yearMonth: Int = savedStateHandle["yearMonth"] ?: 0

    val uiState: StateFlow<BillListUiState> = billRepo.getBillsForMonth(yearMonth)
        .map { bills -> BillListUiState(yearMonth = yearMonth, bills = bills, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BillListUiState(yearMonth = yearMonth))

    private val _generating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _generating.asStateFlow()

    private val _generateError = MutableStateFlow<String?>(null)
    val generateError: StateFlow<String?> = _generateError.asStateFlow()

    fun generateBills() {
        viewModelScope.launch {
            _generating.value = true
            _generateError.value = null
            try {
                val rate = rateRepo.getRateForMonth(yearMonth)
                    ?: rateRepo.getLatestRateOnce()
                if (rate == null) {
                    _generateError.value = "ยังไม่ได้ตั้งค่าอัตราค่าไฟ-น้ำ"
                    return@launch
                }

                val readings = meterReadingDao.getReadingsForMonthOnce(yearMonth)
                val allFees  = rateRepo.getAllFixedFeesOnce()

                val year = yearMonth / 100
                val month = yearMonth % 100
                val dueCal = Calendar.getInstance().apply {
                    set(year, month - 1, 10, 23, 59, 59)
                }

                for (reading in readings) {
                    val room   = roomRepo.getRoomByIdOnce(reading.roomId) ?: continue
                    val tenant = tenantRepo.getActiveTenantForRoomOnce(reading.roomId) ?: continue

                    val existing = billRepo.getBillsForMonth(yearMonth).first()
                        .find { it.roomId == reading.roomId && it.status != BillStatus.VOID }
                    if (existing != null) continue

                    val elecUnits  = reading.electricityUnits
                    val elecAmount = maxOf(
                        rate.electricityMinimum,
                        elecUnits * rate.electricityRatePerUnit
                    )

                    val waterUnits  = reading.waterUnits
                    val waterAmount = if (rate.waterFlatPerPerson != null) {
                        rate.waterFlatPerPerson * reading.numberOfPeople
                    } else {
                        maxOf(rate.waterMinimum, waterUnits * rate.waterRatePerUnit)
                    }

                    val roomFees = allFees.filter { fee ->
                        fee.appliedToAll || rateRepo.getFeesForRoom(room.id).any { it.id == fee.id }
                    }
                    val fixedFeesTotal = roomFees.sumOf { it.amount }

                    val total = room.monthlyRent + elecAmount + waterAmount + fixedFeesTotal
                    val billNumber = billRepo.generateNextBillNumber(yearMonth)

                    val bill = Bill(
                        billNumber   = billNumber,
                        roomId       = room.id,
                        tenantId     = tenant.id,
                        tenantName   = tenant.name,
                        tenantPhone  = tenant.phone,
                        tenantLineName = tenant.lineName,
                        roomNumber   = room.number,
                        buildingName = "",
                        yearMonth    = yearMonth,
                        dueDate      = dueCal.timeInMillis,
                        rent         = room.monthlyRent,
                        electricityPrevious = reading.effectiveElectricityPrevious,
                        electricityCurrent  = reading.electricityCurrent,
                        electricityUnits    = elecUnits,
                        electricityAmount   = elecAmount,
                        waterPrevious = reading.effectiveWaterPrevious,
                        waterCurrent  = reading.waterCurrent,
                        waterUnits    = waterUnits,
                        waterAmount   = waterAmount,
                        fixedFeesTotal = fixedFeesTotal,
                        total         = total,
                        status        = BillStatus.DRAFT,
                    )
                    billRepo.saveBill(bill)
                }
            } catch (e: Exception) {
                _generateError.value = "เกิดข้อผิดพลาด: ${e.message}"
            } finally {
                _generating.value = false
            }
        }
    }

    fun issueBill(bill: Bill) {
        viewModelScope.launch {
            billRepo.saveBill(bill.copy(status = BillStatus.ISSUED))
        }
    }

    fun issueAllDrafts() {
        viewModelScope.launch {
            uiState.value.bills
                .filter { it.status == BillStatus.DRAFT }
                .forEach { billRepo.saveBill(it.copy(status = BillStatus.ISSUED)) }
        }
    }
}
