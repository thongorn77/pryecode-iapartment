/*
 * DashboardViewModel.kt — ViewModel ของหน้าหลัก (Dashboard)
 * ============================================================
 * ดึงข้อมูลสรุปสำหรับเดือนปัจจุบัน:
 *   - จำนวนห้องทั้งหมด / ว่าง / มีผู้เช่า
 *   - ยอดบิลเดือนนี้ / เก็บได้แล้ว / ค้างชำระ
 *   - สัญญาที่จะหมดใน 30 วัน
 * combine() รวม Flow หลายตัวเป็น state เดียว
 */
package com.dormhub.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.entity.BillStatus
import com.dormhub.app.data.entity.RoomStatus
import com.dormhub.app.data.entity.Tenant
import com.dormhub.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val propertyName: String = "",
    val currentYearMonth: Int = 0,
    val totalRooms: Int = 0,
    val occupiedRooms: Int = 0,
    val vacantRooms: Int = 0,
    val totalBilled: Long = 0L,
    val totalCollected: Long = 0L,
    val expiringContracts: List<Tenant> = emptyList(),
    val lastBackupDaysAgo: Int? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val propertyRepo: PropertyRepository,
    private val roomRepo: RoomRepository,
    private val tenantRepo: TenantRepository,
    private val billRepo: BillRepository,
) : ViewModel() {

    private val today = LocalDate.now()
    private val currentYearMonth = today.year * 100 + today.monthValue

    val uiState: StateFlow<DashboardUiState> = combine(
        propertyRepo.getSettings(),
        roomRepo.getTotalRoomCount(),
        roomRepo.getRoomCountByStatus(RoomStatus.OCCUPIED),
        roomRepo.getRoomCountByStatus(RoomStatus.VACANT),
        billRepo.getTotalBilledForMonth(currentYearMonth),
        billRepo.getTotalPaidForMonth(currentYearMonth),
        tenantRepo.getContractsEndingSoon(30)
    ) { values ->
        val settings    = values[0] as? com.dormhub.app.data.entity.PropertySettings
        val total       = values[1] as Int
        val occupied    = values[2] as Int
        val vacant      = values[3] as Int
        val billed      = (values[4] as? Long) ?: 0L
        val collected   = (values[5] as? Long) ?: 0L
        @Suppress("UNCHECKED_CAST")
        val expiring    = values[6] as List<Tenant>
        DashboardUiState(
            propertyName      = settings?.name ?: "",
            currentYearMonth  = currentYearMonth,
            totalRooms        = total,
            occupiedRooms     = occupied,
            vacantRooms       = vacant,
            totalBilled       = billed,
            totalCollected    = collected,
            expiringContracts = expiring
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
