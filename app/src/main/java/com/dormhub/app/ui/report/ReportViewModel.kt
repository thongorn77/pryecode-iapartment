package com.dormhub.app.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.dao.MonthSummary
import com.dormhub.app.data.repository.BillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ReportUiState(
    val monthlySummaries: List<MonthSummary> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val billRepo: BillRepository,
) : ViewModel() {

    val uiState: StateFlow<ReportUiState> = billRepo.getMonthlyReport()
        .map { summaries -> ReportUiState(monthlySummaries = summaries, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportUiState())
}
