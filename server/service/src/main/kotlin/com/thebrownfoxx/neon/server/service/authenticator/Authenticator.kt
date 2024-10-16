package com.thebrownfoxx.neon.server.service.authenticator

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.server.service.authenticator.model.AuthenticationError

interface Authenticator {
    suspend fun authenticate(memberId: MemberId): Result<Boolean, AuthenticationError>
}