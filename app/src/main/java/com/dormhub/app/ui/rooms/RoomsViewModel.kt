/*
 * RoomsViewModel.kt — ViewModel ของหน้ารายการห้องพัก
 * ============================================================
 * จัดกลุ่มห้องตาม building + floor เพื่อแสดงเป็น grid
 * RoomsUiState.grouped = Map<buildingName, Map<floorNumber, List<Room>>>
 * filter ใช้กรองห้องตามสถานะ (ว่าง/มีผู้เช่า/ซ่อม)
 */
package com.dormhub.app.ui.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.entity.Floor
import com.dormhub.app.data.entity.Room
import com.dormhub.app.data.entity.RoomStatus
import com.dormhub.app.data.entity.Building
import com.dormhub.app.data.repository.BuildingRepository
import com.dormhub.app.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class FloorWithRooms(
    val floor: Floor,
    val rooms: List<Room>
)

data class BuildingWithFloors(
    val building: Building,
    val floors: List<FloorWithRooms>
)

data class RoomsUiState(
    val grouped: List<BuildingWithFloors> = emptyList(),
    val statusFilter: RoomStatus? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class RoomsViewModel @Inject constructor(
    private val roomRepo: RoomRepository,
    private val buildingRepo: BuildingRepository,
) : ViewModel() {

    private val _statusFilter = MutableStateFlow<RoomStatus?>(null)

    val uiState: StateFlow<RoomsUiState> = combine(
        roomRepo.getAllRooms(),
        buildingRepo.getAllBuildings(),
        _statusFilter
    ) { rooms, buildings, filter ->
        val filteredRooms = if (filter == null) rooms else rooms.filter { it.status == filter }

        // ดึง floors จาก DB ครั้งเดียว (suspend → ใช้ Flow ไม่ได้โดยตรง, ใช้ flatMapLatest)
        val grouped = buildings
            .filter { !it.isDeleted }
            .map { building ->
                val floorsForBuilding = buildingRepo.getAllFloorsOnce()
                    .filter { it.buildingId == building.id && !it.isDeleted }
                    .sortedBy { it.number }
                BuildingWithFloors(
                    building = building,
                    floors   = floorsForBuilding.map { floor ->
                        FloorWithRooms(
                            floor = floor,
                            rooms = filteredRooms.filter { it.floorId == floor.id }
                                                 .sortedBy { it.number }
                        )
                    }
                )
            }

        RoomsUiState(grouped = grouped, statusFilter = filter, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoomsUiState())

    fun setStatusFilter(status: RoomStatus?) = _statusFilter.update { status }
}
