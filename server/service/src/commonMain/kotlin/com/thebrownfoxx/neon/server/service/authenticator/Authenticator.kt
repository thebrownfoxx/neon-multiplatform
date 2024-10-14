package com.thebrownfoxx.neon.server.service.authenticator

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.service.authenticator.model.LoginError
import kotlinx.coroutines.flow.StateFlow

interface Authenticator {
    val loggedInMemberId: StateFlow<MemberId?>
    val loggedIn: StateFlow<Boolean>

    suspend fun login(username: String, password: String): UnitResult<LoginError>
}