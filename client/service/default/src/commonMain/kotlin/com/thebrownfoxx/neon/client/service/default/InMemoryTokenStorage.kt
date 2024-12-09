package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.TokenStorage
import com.thebrownfoxx.neon.client.service.TokenStorage.GetTokenError
import com.thebrownfoxx.neon.client.service.TokenStorage.SetTokenUnexpectedError
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.UnitSuccess
import com.thebrownfoxx.outcome.memberBlockContext

class InMemoryTokenStorage : TokenStorage {
    private var token: Jwt? = null

    override suspend fun get(): Outcome<Jwt, GetTokenError> {
        memberBlockContext("get") {
            return token?.let { Success(it) } ?: Failure(GetTokenError.NoTokenSaved)
        }
    }

    override suspend fun set(token: Jwt): UnitOutcome<SetTokenUnexpectedError> {
        this.token = token
        return UnitSuccess
    }

    override suspend fun clear(): UnitOutcome<SetTokenUnexpectedError> {
        this.token = null
        return UnitSuccess
    }
}