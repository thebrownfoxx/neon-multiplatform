package com.thebrownfoxx.neon.client.service.authenticator

import com.thebrownfoxx.neon.client.service.authenticator.model.LoginError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.UnitOutcome
import kotlinx.coroutines.flow.MutableStateFlow

interface Authenticator {
    val loggedInMember: MutableStateFlow<MemberId?>
    suspend fun login(username: String, password: String): UnitOutcome<LoginError>
}