package com.thebrownfoxx.neon.server.service

import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome

interface Authenticator {
    suspend fun exists(memberId: MemberId): Outcome<Boolean, UserExistsUnexpectedError>
    
    suspend fun login(username: String, password: String): Outcome<MemberId, LoginError>

    data object UserExistsUnexpectedError

    enum class LoginError {
        InvalidCredentials,
        UnexpectedError,
    }
}