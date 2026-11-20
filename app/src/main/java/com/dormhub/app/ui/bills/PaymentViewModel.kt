/*
 * PaymentViewModel.kt — ViewModel สำหรับบันทึกการชำระเงิน
 */
package com.dormhub.app.ui.bills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.entity.Bill
import com.dormhub.app.data.entity.PaymentMethod
import com.dormhub.app.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val bill: Bill? = null,
    val amount: String = "",
    val method: PaymentMethod = PaymentMethod.CASH,
    val note: String = "",
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val billRepo: BillRepository,
) : ViewModel() {

    private val billId: String = savedStateHandle["billId"] ?: ""

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            billRepo.getBillById(billId).collect { bill ->
                _uiState.update { state ->
                    state.copy(
                        bill = bill,
                        amount = if (bill != null) (bill.remainingAmount / 100).toString() else "",
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun setAmount(value: String)         { _uiState.update { it.copy(amount = value) } }
    fun setMethod(value: PaymentMethod)  { _uiState.update { it.copy(method = value) } }
    fun setNote(value: String)           { _uiState.update { it.copy(note = value) } }

    fun save() {
        val state = _uiState.value
        val bill  = state.bill ?: return
        val amountBaht = state.amount.toLongOrNull() ?: run {
            _uiState.update { it.copy(error = "กรุณากรอกจำนวนเงิน") }
            return
        }
        if (amountBaht <= 0) {
            _uiState.update { it.copy(error = "จำนวนเงินต้องมากกว่า 0") }
            return
        }
        val amountSatang = amountBaht * 100L
        viewModelScope.launch {
            try {
                billRepo.recordPayment(
                    bill        = bill,
                    amount      = amountSatang,
                    method      = state.method,
                    note        = state.note,
                    paymentDate = System.currentTimeMillis()
                )
                _uiState.update { it.copy(saveSuccess = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "บันทึกไม่สำเร็จ: ${e.message}") }
            }
        }
    }
}
