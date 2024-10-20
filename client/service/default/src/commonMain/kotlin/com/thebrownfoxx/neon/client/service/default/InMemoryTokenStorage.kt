package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.jwt.TokenStorage
import com.thebrownfoxx.neon.client.service.jwt.model.GetTokenError
import com.thebrownfoxx.neon.client.service.jwt.model.SetTokenError
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.Jwt
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.common.model.unitSuccess

class InMemoryTokenStorage : TokenStorage {
    private var token: Jwt? = null

    override suspend fun get(): Result<Jwt, GetTokenError> {
        return token?.let { Success(it) } ?: Failure(GetTokenError.NoTokenSaved)
    }

    override suspend fun set(token: Jwt): UnitResult<SetTokenError> {
        this.token = token
        return unitSuccess()
    }

    override suspend fun delete(): UnitResult<SetTokenError> {
        this.token = null
        return unitSuccess()
    }
}