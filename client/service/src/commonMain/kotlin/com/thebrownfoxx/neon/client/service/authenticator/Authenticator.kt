package com.thebrownfoxx.neon.client.service.authenticator

import com.thebrownfoxx.neon.client.service.authenticator.model.LoginError
import com.thebrownfoxx.neon.client.service.authenticator.model.LogoutError
import com.thebrownfoxx.neon.common.outcome.UnitOutcome
import com.thebrownfoxx.neon.common.type.id.MemberId
import kotlinx.coroutines.flow.StateFlow

interface Authenticator {
    val loggedIn: StateFlow<Boolean>
    val loggedInMember: StateFlow<MemberId?>
    suspend fun login(username: String, password: String): UnitOutcome<LoginError>
    suspend fun logout(): UnitOutcome<LogoutError>
}