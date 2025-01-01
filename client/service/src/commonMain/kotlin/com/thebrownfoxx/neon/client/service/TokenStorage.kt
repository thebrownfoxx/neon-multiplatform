package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.StateFlow

interface TokenStorage {
    val token: StateFlow<Outcome<Jwt, GetTokenError>>
    suspend fun set(token: Jwt): UnitOutcome<SetTokenUnexpectedError>
    suspend fun clear(): UnitOutcome<SetTokenUnexpectedError>

    enum class GetTokenError {
        NoTokenSaved,
        ConnectionError,
    }

    data object SetTokenUnexpectedError
}