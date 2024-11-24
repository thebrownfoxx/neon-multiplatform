package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.jwt.TokenStorage
import com.thebrownfoxx.neon.client.service.jwt.model.GetTokenError
import com.thebrownfoxx.neon.client.service.jwt.model.SetTokenError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.unitSuccess

class InMemoryTokenStorage : TokenStorage {
    private var token: Jwt? = null

    override suspend fun get(): Outcome<Jwt, GetTokenError> {
        return token?.let { Success(it) } ?: Failure(GetTokenError.NoTokenSaved)
    }

    override suspend fun set(token: Jwt): UnitOutcome<SetTokenError> {
        this.token = token
        return unitSuccess()
    }

    override suspend fun delete(): UnitOutcome<SetTokenError> {
        this.token = null
        return unitSuccess()
    }
}