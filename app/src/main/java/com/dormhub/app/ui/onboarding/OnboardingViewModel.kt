/*
 * OnboardingViewModel.kt — ViewModel สำหรับ Onboarding Wizard
 * ============================================================
 * จัดการ state ของ wizard ทั้ง 5 ขั้นตอน
 * เมื่อกด "เสร็จ" จะบันทึกข้อมูลลง DB ในครั้งเดียว (@Transaction)
 * OnboardingUiState เก็บข้อมูล input ทั้งหมดชั่วคราว
 */
package com.dormhub.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.entity.*
import com.dormhub.app.data.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class BuildingInput(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val floors: Int = 1
)

data class RoomBulkInput(
    val buildingId: String,
    val buildingName: String,
    val floorNumber: Int,
    val floorId: String,
    val pattern: String = "",   // เช่น "101-110"
    val roomType: String = "",
    val rent: String = "3000"
)

data class OnboardingUiState(
    val step: Int = 0,
    // Step 1: ข้อมูลหอพัก
    val propertyName: String = "",
    val propertyAddress: String = "",
    val propertyPhone: String = "",
    val logoPath: String? = null,
    // Step 2: อาคาร
    val buildings: List<BuildingInput> = listOf(BuildingInput(name = "อาคาร A")),
    // Step 3: ห้องพัก (bulk)
    val roomInputs: List<RoomBulkInput> = emptyList(),
    // Step 4: อัตราค่าไฟ-น้ำ
    val electricityRate: String = "5.00",
    val electricityMin: String = "50.00",
    val waterRate: String = "18.00",
    val waterMin: String = "50.00",
    val feeItems: List<Pair<String, String>> = emptyList(), // name, amount (THB)
    // Step 5: PromptPay / บัญชีธนาคาร
    val promptPayId: String = "",
    val bankName: String = "",
    val bankAccountName: String = "",
    val bankAccountNumber: String = "",
    val billingDay: String = "1",
    val dueDay: String = "15",
    // State
    val isLoading: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val propertyRepo: PropertyRepository,
    private val buildingRepo: BuildingRepository,
    private val roomRepo: RoomRepository,
    private val rateRepo: RateRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state = _state.asStateFlow()

    fun updatePropertyName(v: String)    = _state.update { it.copy(propertyName = v) }
    fun updatePropertyAddress(v: String) = _state.update { it.copy(propertyAddress = v) }
    fun updatePropertyPhone(v: String)   = _state.update { it.copy(propertyPhone = v) }

    fun addBuilding() = _state.update {
        val idx = it.buildings.size + 1
        it.copy(buildings = it.buildings + BuildingInput(name = "อาคาร ${('A' + idx - 1)}"))
    }

    fun updateBuilding(index: Int, name: String, floors: Int) = _state.update { s ->
        val updated = s.buildings.toMutableList()
        if (index < updated.size) updated[index] = updated[index].copy(name = name, floors = maxOf(1, floors))
        s.copy(buildings = updated)
    }

    fun removeBuilding(index: Int) = _state.update { s ->
        s.copy(buildings = s.buildings.filterIndexed { i, _ -> i != index })
    }

    // สร้าง RoomBulkInput จาก buildings ที่กรอกไว้ใน Step 2
    fun buildRoomInputs() {
        val rooms = _state.value.buildings.flatMap { building ->
            (1..building.floors).map { floor ->
                RoomBulkInput(
                    buildingId   = building.id,
                    buildingName = building.name,
                    floorNumber  = floor,
                    floorId      = UUID.randomUUID().toString(),
                    pattern      = ""
                )
            }
        }
        _state.update { it.copy(roomInputs = rooms) }
    }

    fun updateRoomInput(index: Int, pattern: String, rent: String) = _state.update { s ->
        val updated = s.roomInputs.toMutableList()
        if (index < updated.size) updated[index] = updated[index].copy(pattern = pattern, rent = rent)
        s.copy(roomInputs = updated)
    }

    fun updateElectricityRate(v: String) = _state.update { it.copy(electricityRate = v) }
    fun updateElectricityMin(v: String)  = _state.update { it.copy(electricityMin = v) }
    fun updateWaterRate(v: String)       = _state.update { it.copy(waterRate = v) }
    fun updateWaterMin(v: String)        = _state.update { it.copy(waterMin = v) }

    fun addFeeItem()                               = _state.update { it.copy(feeItems = it.feeItems + ("" to "")) }
    fun updateFeeItem(i: Int, name: String, amt: String) = _state.update { s ->
        val updated = s.feeItems.toMutableList()
        if (i < updated.size) updated[i] = name to amt
        s.copy(feeItems = updated)
    }
    fun removeFeeItem(i: Int) = _state.update { s ->
        s.copy(feeItems = s.feeItems.filterIndexed { idx, _ -> idx != i })
    }

    fun updatePromptPayId(v: String)        = _state.update { it.copy(promptPayId = v) }
    fun updateBankName(v: String)           = _state.update { it.copy(bankName = v) }
    fun updateBankAccountName(v: String)    = _state.update { it.copy(bankAccountName = v) }
    fun updateBankAccountNumber(v: String)  = _state.update { it.copy(bankAccountNumber = v) }
    fun updateBillingDay(v: String)         = _state.update { it.copy(billingDay = v) }
    fun updateDueDay(v: String)             = _state.update { it.copy(dueDay = v) }

    fun nextStep() = _state.update { it.copy(step = it.step + 1, error = null) }
    fun prevStep() = _state.update { if (it.step > 0) it.copy(step = it.step - 1) else it }

    fun completeOnboarding() {
        val s = _state.value
        if (s.propertyName.isBlank()) {
            _state.update { it.copy(error = "กรุณากรอกชื่อหอพัก") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                // 1. บันทึก PropertySettings
                val today = LocalDate.now()
                val yearMonth = today.year * 100 + today.monthValue
                propertyRepo.saveSettings(PropertySettings(
                    name              = s.propertyName.trim(),
                    address           = s.propertyAddress.trim(),
                    phone             = s.propertyPhone.trim(),
                    promptPayId       = s.promptPayId.trim(),
                    bankName          = s.bankName.trim(),
                    bankAccountName   = s.bankAccountName.trim(),
                    bankAccountNumber = s.bankAccountNumber.trim(),
                    billingDay        = s.billingDay.toIntOrNull() ?: 1,
                    dueDay            = s.dueDay.toIntOrNull() ?: 15,
                ))

                // 2. บันทึก Buildings + Floors
                val buildingIdMap = mutableMapOf<String, String>()
                s.buildings.forEach { bInput ->
                    buildingRepo.saveBuilding(Building(id = bInput.id, name = bInput.name.ifBlank { "อาคาร" }))
                    buildingIdMap[bInput.id] = bInput.id
                }

                // 3. บันทึก Rooms (จาก pattern เช่น "101-110")
                s.roomInputs.forEach { rInput ->
                    val floor = Floor(id = rInput.floorId, buildingId = rInput.buildingId, number = rInput.floorNumber)
                    buildingRepo.saveFloor(floor)
                    val rooms = parseRoomPattern(rInput)
                    if (rooms.isNotEmpty()) roomRepo.saveRooms(rooms)
                }

                // 4. บันทึก Rate
                val elecRate = (s.electricityRate.toDoubleOrNull() ?: 5.0 * 100).toLong()
                    .let { if (it == 0L) (s.electricityRate.toDoubleOrNull() ?: 5.0).times(100).toLong() else it }
                val elecRateSatang = (s.electricityRate.toDoubleOrNull() ?: 5.0).times(100).toLong()
                val elecMinSatang  = (s.electricityMin.toDoubleOrNull() ?: 50.0).times(100).toLong()
                val waterRateSatang= (s.waterRate.toDoubleOrNull() ?: 18.0).times(100).toLong()
                val waterMinSatang = (s.waterMin.toDoubleOrNull() ?: 50.0).times(100).toLong()
                rateRepo.saveRate(RateVersion(
                    effectiveYearMonth      = yearMonth,
                    electricityRatePerUnit  = elecRateSatang,
                    electricityMinimum      = elecMinSatang,
                    waterRatePerUnit        = waterRateSatang,
                    waterMinimum            = waterMinSatang,
                ))

                // 5. บันทึก Fixed Fees
                s.feeItems.forEach { (name, amtStr) ->
                    if (name.isNotBlank()) {
                        val amt = (amtStr.toDoubleOrNull() ?: 0.0).times(100).toLong()
                        if (amt > 0) rateRepo.saveFixedFee(FixedFee(name = name, amount = amt))
                    }
                }

                _state.update { it.copy(isLoading = false, isComplete = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "เกิดข้อผิดพลาด: ${e.message}") }
            }
        }
    }

    // แปลง pattern "101-110" หรือ "101,102,103" เป็น Room list
    private fun parseRoomPattern(input: RoomBulkInput): List<Room> {
        val rent = (input.rent.toDoubleOrNull() ?: 0.0).times(100).toLong()
        val pattern = input.pattern.trim()
        if (pattern.isBlank()) return emptyList()

        return when {
            pattern.contains("-") -> {
                val parts = pattern.split("-")
                val start = parts[0].trim().toIntOrNull() ?: return emptyList()
                val end   = parts[1].trim().toIntOrNull() ?: return emptyList()
                (start..end).map { num ->
                    Room(buildingId = input.buildingId, floorId = input.floorId,
                         number = num.toString(), monthlyRent = rent)
                }
            }
            pattern.contains(",") -> {
                pattern.split(",").mapNotNull { it.trim().ifBlank { null } }.map { num ->
                    Room(buildingId = input.buildingId, floorId = input.floorId,
                         number = num, monthlyRent = rent)
                }
            }
            else -> listOf(Room(buildingId = input.buildingId, floorId = input.floorId,
                                number = pattern, monthlyRent = rent))
        }
    }
}
