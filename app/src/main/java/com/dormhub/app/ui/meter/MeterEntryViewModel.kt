/*
 * MeterEntryViewModel.kt — ViewModel สำหรับจดมิเตอร์รายเดือน
 * ============================================================
 * โหลดรายการห้องที่มีผู้เช่า พร้อม pre-fill ค่ามิเตอร์เดือนก่อน
 * บันทึก MeterReading ต่อห้อง
 */
package com.dormhub.app.ui.meter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.dao.MeterReadingDao
import com.dormhub.app.data.entity.MeterReading
import com.dormhub.app.data.entity.Room
import com.dormhub.app.data.entity.Tenant
import com.dormhub.app.data.repository.RoomRepository
import com.dormhub.app.data.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomMeterState(
    val room: Room,
    val tenant: Tenant?,
    val existingReading: MeterReading?,
    val electricityPrevious: String = "",
    val electricityCurrent: String = "",
    val waterPrevious: String = "",
    val waterCurrent: String = "",
    val isSaved: Boolean = false,
)

data class MeterEntryUiState(
    val yearMonth: Int = 0,
    val rooms: List<RoomMeterState> = emptyList(),
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class MeterEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val roomRepo: RoomRepository,
    private val tenantRepo: TenantRepository,
    private val meterDao: MeterReadingDao,
) : ViewModel() {

    val yearMonth: Int = savedStateHandle["yearMonth"] ?: 0

    private val _uiState = MutableStateFlow(MeterEntryUiState(yearMonth = yearMonth))
    val uiState: StateFlow<MeterEntryUiState> = _uiState.asStateFlow()

    init {
        loadRooms()
    }

    private fun loadRooms() {
        viewModelScope.launch {
            roomRepo.getAllRooms()
                .collect { allRooms ->
                    val occupiedRooms = allRooms.filter { !it.isDeleted }
                    val states = occupiedRooms.map { room ->
                        val tenant = tenantRepo.getActiveTenantForRoomOnce(room.id)
                        val existing = meterDao.getReadingForRoomMonthOnce(room.id, yearMonth)
                        val latest = if (existing == null) meterDao.getLatestReadingForRoom(room.id) else null

                        val prevElec = existing?.electricityPrevious ?: latest?.electricityCurrent ?: 0L
                        val curElec  = existing?.electricityCurrent ?: 0L
                        val prevWater = existing?.waterPrevious ?: latest?.waterCurrent ?: 0L
                        val curWater  = existing?.waterCurrent ?: 0L

                        RoomMeterState(
                            room = room,
                            tenant = tenant,
                            existingReading = existing,
                            electricityPrevious = if (prevElec > 0) prevElec.toString() else "",
                            electricityCurrent  = if (curElec > 0) curElec.toString() else "",
                            waterPrevious = if (prevWater > 0) prevWater.toString() else "",
                            waterCurrent  = if (curWater > 0) curWater.toString() else "",
                            isSaved = existing != null,
                        )
                    }
                    _uiState.update { it.copy(rooms = states, isLoading = false) }
                }
        }
    }

    fun updateElecCurrent(roomId: String, value: String) {
        _uiState.update { state ->
            state.copy(rooms = state.rooms.map { r ->
                if (r.room.id == roomId) r.copy(electricityCurrent = value) else r
            })
        }
    }

    fun updateWaterCurrent(roomId: String, value: String) {
        _uiState.update { state ->
            state.copy(rooms = state.rooms.map { r ->
                if (r.room.id == roomId) r.copy(waterCurrent = value) else r
            })
        }
    }

    fun saveReading(roomId: String) {
        val roomState = _uiState.value.rooms.find { it.room.id == roomId } ?: return
        val elecPrev = roomState.electricityPrevious.toLongOrNull() ?: 0L
        val elecCur  = roomState.electricityCurrent.toLongOrNull() ?: return
        val waterPrev = roomState.waterPrevious.toLongOrNull() ?: 0L
        val waterCur  = roomState.waterCurrent.toLongOrNull() ?: return

        viewModelScope.launch {
            val reading = roomState.existingReading?.copy(
                electricityPrevious = elecPrev,
                electricityCurrent  = elecCur,
                waterPrevious = waterPrev,
                waterCurrent  = waterCur,
                updatedAt = System.currentTimeMillis()
            ) ?: MeterReading(
                roomId = roomId,
                yearMonth = yearMonth,
                electricityPrevious = elecPrev,
                electricityCurrent  = elecCur,
                waterPrevious = waterPrev,
                waterCurrent  = waterCur,
            )
            meterDao.insert(reading)
            _uiState.update { state ->
                state.copy(rooms = state.rooms.map { r ->
                    if (r.room.id == roomId) r.copy(isSaved = true, existingReading = reading) else r
                })
            }
        }
    }
}
