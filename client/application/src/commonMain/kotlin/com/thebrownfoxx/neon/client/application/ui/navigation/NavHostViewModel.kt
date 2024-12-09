package com.thebrownfoxx.neon.client.application.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thebrownfoxx.neon.client.service.Authenticator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class NavHostViewModel(authenticator: Authenticator) : ViewModel() {
    val loggedIn = authenticator.loggedInMember.map { it != null }
        .stateIn(scope = viewModelScope, started = SharingStarted.Lazily, initialValue = false)
}