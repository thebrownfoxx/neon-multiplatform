package com.thebrownfoxx.neon.client.service.authenticator

import com.thebrownfoxx.neon.client.service.authenticator.model.LoginError
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.UnitResult
import kotlinx.coroutines.flow.StateFlow

interface Authenticator {
    val loggedInMemberId: StateFlow<MemberId?>
    val loggedIn: StateFlow<Boolean>

    suspend fun login(username: String, password: String): UnitResult<LoginError>
}