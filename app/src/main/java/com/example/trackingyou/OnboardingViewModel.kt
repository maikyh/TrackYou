package com.example.trackingyou

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val PREFS_NAME = "tracking_you_prefs"
        private const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
    }

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isOnboardingShown = MutableStateFlow(false)
    val isOnboardingShown: StateFlow<Boolean> = _isOnboardingShown

    init {
        viewModelScope.launch {
            _isOnboardingShown.value = prefs.getBoolean(KEY_ONBOARDING_SHOWN, false)
        }
    }

    fun setOnboardingShown() {
        viewModelScope.launch {
            prefs.edit().putBoolean(KEY_ONBOARDING_SHOWN, true).apply()
            _isOnboardingShown.value = true
        }
    }
}
