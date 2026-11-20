/*
 * AppViewModel.kt — ViewModel ระดับ App
 * ============================================================
 * ตรวจสอบว่า onboarding เสร็จหรือยัง เพื่อกำหนด startDestination
 * isOnboardingComplete: null = กำลังโหลด, true = ไปหน้าหลัก, false = ไป onboarding
 */
package com.dormhub.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dormhub.app.data.repository.PropertyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    propertyRepository: PropertyRepository
) : ViewModel() {

    // null = loading, true = onboarded, false = need onboarding
    val isOnboardingComplete = propertyRepository.getSettings()
        .map { settings -> settings?.name?.isNotBlank() == true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
