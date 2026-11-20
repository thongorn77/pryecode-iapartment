/*
 * TenantViewModel.kt — ViewModel สำหรับฟอร์มเพิ่ม/แก้ไขผู้เช่า
 * ============================================================
 * saveTenant() จะ:
 *   1. บันทึก/อัปเดต Tenant record
 *   2. อัปเดต Room.status = OCCUPIED และ currentTenantId
 */
package com.dormhub.app.ui.tenants

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.entity.BillLanguage
import com.dormhub.app.data.entity.RoomStatus
import com.dormhub.app.data.entity.Tenant
import com.dormhub.app.data.repository.RoomRepository
import com.dormhub.app.data.repository.TenantRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class TenantFormState(
    val roomId: String = "",
    val tenantId: String? = null,
    val name: String = "",
    val phone: String = "",
    val lineName: String = "",
    val moveInDate: String = "",    // dd/MM/yyyy
    val contractEnd: String = "",   // dd/MM/yyyy (optional)
    val deposit: String = "0",
    val advanceRent: String = "0",
    val billLanguage: BillLanguage = BillLanguage.TH,
    val note: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class TenantViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tenantRepo: TenantRepository,
    private val roomRepo: RoomRepository,
) : ViewModel() {

    private val roomId: String = savedStateHandle["roomId"] ?: ""
    private val tenantId: String? = savedStateHandle["tenantId"]
    private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("th"))

    private val _state = MutableStateFlow(TenantFormState(
        roomId   = roomId,
        tenantId = tenantId,
        moveInDate = sdf.format(Date())
    ))
    val state = _state.asStateFlow()

    init {
        if (tenantId != null) loadTenant(tenantId)
    }

    private fun loadTenant(id: String) {
        viewModelScope.launch {
            val tenant = tenantRepo.getTenantByIdOnce(id) ?: return@launch
            _state.update {
                it.copy(
                    name         = tenant.name,
                    phone        = tenant.phone,
                    lineName     = tenant.lineName,
                    moveInDate   = sdf.format(Date(tenant.moveInDate)),
                    contractEnd  = tenant.contractEnd?.let { d -> sdf.format(Date(d)) } ?: "",
                    deposit      = (tenant.deposit / 100).toString(),
                    advanceRent  = (tenant.advanceRent / 100).toString(),
                    billLanguage = tenant.billLanguage,
                    note         = tenant.note,
                )
            }
        }
    }

    fun updateName(v: String)        = _state.update { it.copy(name = v) }
    fun updatePhone(v: String)       = _state.update { it.copy(phone = v) }
    fun updateLineName(v: String)    = _state.update { it.copy(lineName = v) }
    fun updateMoveInDate(v: String)  = _state.update { it.copy(moveInDate = v) }
    fun updateContractEnd(v: String) = _state.update { it.copy(contractEnd = v) }
    fun updateDeposit(v: String)     = _state.update { it.copy(deposit = v) }
    fun updateAdvanceRent(v: String) = _state.update { it.copy(advanceRent = v) }
    fun updateBillLanguage(v: BillLanguage) = _state.update { it.copy(billLanguage = v) }
    fun updateNote(v: String)        = _state.update { it.copy(note = v) }

    fun save() {
        val s = _state.value
        if (s.name.isBlank()) {
            _state.update { it.copy(error = "กรุณากรอกชื่อผู้เช่า") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val moveInMs = sdf.parse(s.moveInDate)?.time ?: System.currentTimeMillis()
                val contractEndMs = if (s.contractEnd.isNotBlank()) {
                    sdf.parse(s.contractEnd)?.time
                } else null

                val tenant = Tenant(
                    id            = s.tenantId ?: UUID.randomUUID().toString(),
                    roomId        = s.roomId,
                    name          = s.name.trim(),
                    phone         = s.phone.trim(),
                    lineName      = s.lineName.trim(),
                    moveInDate    = moveInMs,
                    contractStart = moveInMs,
                    contractEnd   = contractEndMs,
                    deposit       = (s.deposit.toDoubleOrNull() ?: 0.0).times(100).toLong(),
                    advanceRent   = (s.advanceRent.toDoubleOrNull() ?: 0.0).times(100).toLong(),
                    billLanguage  = s.billLanguage,
                    note          = s.note,
                    isActive      = true,
                )
                tenantRepo.saveTenant(tenant)
                roomRepo.updateRoomStatus(s.roomId, RoomStatus.OCCUPIED, tenant.id)
                _state.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "เกิดข้อผิดพลาด: ${e.message}") }
            }
        }
    }
}
