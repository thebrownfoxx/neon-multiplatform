package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.common.model.LoginResult
import com.thebrownfoxx.neon.common.model.MemberId
import kotlinx.coroutines.flow.StateFlow

interface Authenticator {
    val loggedInMember: StateFlow<MemberId?>
    val loggedIn: StateFlow<Boolean>

    suspend fun login(username: String, password: String): LoginResult
}