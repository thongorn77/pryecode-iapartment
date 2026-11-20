package com.dormhub.app.ui.bills

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.entity.Bill
import com.dormhub.app.data.entity.BillItem
import com.dormhub.app.data.entity.BillStatus
import com.dormhub.app.data.entity.Payment
import com.dormhub.app.data.entity.PropertySettings
import com.dormhub.app.data.repository.BillRepository
import com.dormhub.app.data.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillDetailUiState(
    val bill: Bill? = null,
    val payments: List<Payment> = emptyList(),
    val billItems: List<BillItem> = emptyList(),
    val settings: PropertySettings? = null,
    val isLoading: Boolean = true,
    val voidSuccess: Boolean = false,
)

@HiltViewModel
class BillDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val billRepo: BillRepository,
    private val propertyRepo: PropertyRepository,
) : ViewModel() {

    private val billId: String = savedStateHandle["billId"] ?: ""

    val uiState: StateFlow<BillDetailUiState> = combine(
        billRepo.getBillById(billId),
        billRepo.getPaymentsForBill(billId),
        billRepo.getItemsForBill(billId),
        propertyRepo.getSettings()
    ) { bill, payments, items, settings ->
        BillDetailUiState(
            bill = bill,
            payments = payments,
            billItems = items,
            settings = settings,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BillDetailUiState())

    fun issueBill() {
        val bill = uiState.value.bill ?: return
        viewModelScope.launch { billRepo.saveBill(bill.copy(status = BillStatus.ISSUED)) }
    }

    fun voidBill(reason: String) {
        viewModelScope.launch { billRepo.voidBill(billId, reason) }
    }

    fun addItem(name: String, amountBaht: Double) {
        val bill = uiState.value.bill ?: return
        val amountSatang = (amountBaht * 100).toLong()
        viewModelScope.launch {
            val item = BillItem(billId = bill.id, name = name, amount = amountSatang)
            billRepo.saveBillItem(item)
            val newExtrasTotal = bill.extrasTotal + amountSatang
            val newTotal = bill.total + amountSatang
            billRepo.saveBill(bill.copy(extrasTotal = newExtrasTotal, total = newTotal))
        }
    }

    fun removeItem(itemId: String) {
        val bill = uiState.value.bill ?: return
        val item = uiState.value.billItems.find { it.id == itemId } ?: return
        viewModelScope.launch {
            billRepo.deleteBillItem(itemId)
            val newExtrasTotal = bill.extrasTotal - item.amount
            val newTotal = bill.total - item.amount
            billRepo.saveBill(bill.copy(extrasTotal = newExtrasTotal, total = newTotal))
        }
    }
}
