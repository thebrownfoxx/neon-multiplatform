package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.TokenStorage
import com.thebrownfoxx.neon.client.service.TokenStorage.GetTokenError
import com.thebrownfoxx.neon.client.service.TokenStorage.SetTokenUnexpectedError
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryTokenStorage : TokenStorage {
    private val _token =
        MutableStateFlow<Outcome<Jwt, GetTokenError>>(Failure(GetTokenError.NoTokenSaved))
    override val token = _token.asStateFlow()

    override suspend fun set(token: Jwt): UnitOutcome<SetTokenUnexpectedError> {
        _token.value = Success(token)
        return UnitSuccess
    }

    override suspend fun clear(): UnitOutcome<SetTokenUnexpectedError> {
        _token.value = Failure(GetTokenError.NoTokenSaved)
        return UnitSuccess
    }
}