package com.thebrownfoxx.neon.client.application.dummy

import com.thebrownfoxx.neon.client.service.Authenticator
import com.thebrownfoxx.neon.client.service.Authenticator.LoginError
import com.thebrownfoxx.neon.client.service.Authenticator.LogoutError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.plus

class DummyAuthenticator : Authenticator {
    private val coroutineScope = CoroutineScope(Dispatchers.Default) + SupervisorJob()

    private val _loggedInMember = MutableStateFlow<MemberId?>(MemberId())
    override val loggedInMemberId = _loggedInMember.asStateFlow()

    override val loggedIn = _loggedInMember.map { it != null }
        .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    override suspend fun login(
        username: String,
        password: String,
    ): UnitOutcome<LoginError> {
        _loggedInMember.value = MemberId()
        return UnitSuccess
    }

    override suspend fun logout(): UnitOutcome<LogoutError> {
        _loggedInMember.value = null
        return UnitSuccess
    }
}