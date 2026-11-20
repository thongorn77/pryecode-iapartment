/*
 * SettingsViewModel.kt — ViewModel ของหน้าตั้งค่า
 * ============================================================
 * โหลดและบันทึก PropertySettings, Rates, FixedFees
 * Phase 2 จะเพิ่มการตั้งค่า billing, app lock
 * Phase 5 จะเพิ่ม Google Drive backup settings
 */
package com.dormhub.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.entity.FixedFee
import com.dormhub.app.data.entity.PropertySettings
import com.dormhub.app.data.entity.RateVersion
import com.dormhub.app.data.repository.PropertyRepository
import com.dormhub.app.data.repository.RateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SettingsUiState(
    val settings: PropertySettings = PropertySettings(),
    val latestRate: RateVersion? = null,
    val fixedFees: List<FixedFee> = emptyList(),
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val propertyRepo: PropertyRepository,
    private val rateRepo: RateRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        propertyRepo.getSettings(),
        rateRepo.getLatestRate(),
        rateRepo.getAllFixedFees()
    ) { settings, rate, fees ->
        SettingsUiState(
            settings   = settings ?: PropertySettings(),
            latestRate = rate,
            fixedFees  = fees
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun savePropertySettings(settings: PropertySettings) {
        viewModelScope.launch {
            try {
                propertyRepo.saveSettings(settings)
            } catch (e: Exception) {
                // error handling
            }
        }
    }

    fun saveRate(
        elecRate: Double, elecMin: Double,
        waterRate: Double, waterMin: Double
    ) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val yearMonth = today.year * 100 + today.monthValue
            rateRepo.saveRate(RateVersion(
                effectiveYearMonth     = yearMonth,
                electricityRatePerUnit = (elecRate * 100).toLong(),
                electricityMinimum     = (elecMin * 100).toLong(),
                waterRatePerUnit       = (waterRate * 100).toLong(),
                waterMinimum           = (waterMin * 100).toLong(),
            ))
        }
    }

    fun saveFixedFee(name: String, amountBaht: Double) {
        viewModelScope.launch {
            rateRepo.saveFixedFee(FixedFee(name = name, amount = (amountBaht * 100).toLong()))
        }
    }

    fun deleteFixedFee(id: String) {
        viewModelScope.launch { rateRepo.deleteFixedFee(id) }
    }
}
