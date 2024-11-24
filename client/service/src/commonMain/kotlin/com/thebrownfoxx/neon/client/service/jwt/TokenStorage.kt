package com.thebrownfoxx.neon.client.service.jwt

import com.thebrownfoxx.neon.client.service.jwt.model.GetTokenError
import com.thebrownfoxx.neon.client.service.jwt.model.SetTokenError
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome

interface TokenStorage {
    suspend fun get(): Outcome<Jwt, GetTokenError>
    suspend fun set(token: Jwt): UnitOutcome<SetTokenError>
    suspend fun delete(): UnitOutcome<SetTokenError>
}