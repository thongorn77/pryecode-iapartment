/*
 * RoomDetailViewModel.kt — ViewModel ของหน้าข้อมูลห้องพัก
 * ============================================================
 * แสดงข้อมูลห้อง + ผู้เช่าปัจจุบัน + ประวัติบิล
 * moveOut() จะดำเนินการย้ายออก: set tenant isActive=false, set room VACANT
 */
package com.dormhub.app.ui.rooms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.entity.Bill
import com.dormhub.app.data.entity.Room
import com.dormhub.app.data.entity.RoomStatus
import com.dormhub.app.data.entity.Tenant
import com.dormhub.app.data.repository.BillRepository
import com.dormhub.app.data.repository.RoomRepository
import com.dormhub.app.data.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomDetailUiState(
    val room: Room? = null,
    val tenant: Tenant? = null,
    val recentBills: List<Bill> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RoomDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val roomRepo: RoomRepository,
    private val tenantRepo: TenantRepository,
    private val billRepo: BillRepository,
) : ViewModel() {

    private val roomId: String = savedStateHandle["roomId"] ?: ""

    val uiState: StateFlow<RoomDetailUiState> = combine(
        roomRepo.getRoomById(roomId),
        tenantRepo.getActiveTenantForRoom(roomId),
        billRepo.getBillsForRoom(roomId)
    ) { room, tenant, bills ->
        RoomDetailUiState(
            room        = room,
            tenant      = tenant,
            recentBills = bills,
            isLoading   = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoomDetailUiState())

    fun moveOut() {
        val tenant = uiState.value.tenant ?: return
        val room   = uiState.value.room   ?: return
        viewModelScope.launch {
            tenantRepo.deactivateTenant(tenant.id)
            roomRepo.updateRoomStatus(room.id, RoomStatus.VACANT, null)
        }
    }

    fun editRoom(number: String, monthlyRentBaht: Double, status: RoomStatus) {
        val room = uiState.value.room ?: return
        viewModelScope.launch {
            roomRepo.saveRoom(room.copy(
                number = number.trim(),
                monthlyRent = (monthlyRentBaht * 100).toLong(),
                status = status
            ))
        }
    }

    fun deleteRoom(onDeleted: () -> Unit) {
        val room = uiState.value.room ?: return
        viewModelScope.launch {
            roomRepo.deleteRoom(room.id)
            onDeleted()
        }
    }
}
