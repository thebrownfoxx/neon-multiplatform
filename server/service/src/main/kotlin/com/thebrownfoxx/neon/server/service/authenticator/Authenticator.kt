package com.thebrownfoxx.neon.server.service.authenticator

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.server.service.authenticator.model.AuthenticationError
import com.thebrownfoxx.neon.server.service.authenticator.model.LoginError

interface Authenticator {
    suspend fun exists(memberId: MemberId): Outcome<Boolean, AuthenticationError>
    suspend fun login(username: String, password: String): Outcome<MemberId, LoginError>
}