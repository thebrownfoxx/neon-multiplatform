package com.thebrownfoxx.neon.server.service.authenticator

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.server.service.authenticator.model.AuthenticationError
import com.thebrownfoxx.neon.server.service.authenticator.model.LoginError

interface Authenticator {
    suspend fun exists(memberId: MemberId): Result<Boolean, AuthenticationError>
    suspend fun login(username: String, password: String): Result<MemberId, LoginError>
}