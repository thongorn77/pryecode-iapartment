/*
 * BillDetailViewModel.kt — ViewModel สำหรับรายละเอียดบิล
 */
package com.dormhub.app.ui.bills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.entity.Bill
import com.dormhub.app.data.entity.Payment
import com.dormhub.app.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillDetailUiState(
    val bill: Bill? = null,
    val payments: List<Payment> = emptyList(),
    val isLoading: Boolean = true,
    val voidSuccess: Boolean = false,
)

@HiltViewModel
class BillDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val billRepo: BillRepository,
) : ViewModel() {

    private val billId: String = savedStateHandle["billId"] ?: ""

    val uiState: StateFlow<BillDetailUiState> = combine(
        billRepo.getBillById(billId),
        billRepo.getPaymentsForBill(billId)
    ) { bill, payments ->
        BillDetailUiState(bill = bill, payments = payments, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BillDetailUiState())

    fun issueBill() {
        val bill = uiState.value.bill ?: return
        viewModelScope.launch {
            billRepo.saveBill(bill.copy(status = com.dormhub.app.data.entity.BillStatus.ISSUED))
        }
    }

    fun voidBill(reason: String) {
        viewModelScope.launch {
            billRepo.voidBill(billId, reason)
        }
    }
}
