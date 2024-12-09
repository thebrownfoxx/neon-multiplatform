package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome

interface TokenStorage {
    suspend fun get(): Outcome<Jwt, GetTokenError>
    suspend fun set(token: Jwt): UnitOutcome<SetTokenUnexpectedError>
    suspend fun clear(): UnitOutcome<SetTokenUnexpectedError>

    enum class GetTokenError {
        NoTokenSaved,
        ConnectionError,
    }

    data object SetTokenUnexpectedError
}