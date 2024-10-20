package com.thebrownfoxx.neon.client.service.jwt

import com.thebrownfoxx.neon.client.service.jwt.model.GetTokenError
import com.thebrownfoxx.neon.client.service.jwt.model.SetTokenError
import com.thebrownfoxx.neon.common.model.Jwt
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult

interface TokenStorage {
    suspend fun get(): Result<Jwt, GetTokenError>
    suspend fun set(token: Jwt): UnitResult<SetTokenError>
    suspend fun delete(): UnitResult<SetTokenError>
}