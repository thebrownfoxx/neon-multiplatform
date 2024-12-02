package com.thebrownfoxx.neon.client.service.jwt

import com.thebrownfoxx.neon.client.service.jwt.model.GetTokenError
import com.thebrownfoxx.neon.client.service.jwt.model.SetTokenError
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.UnitOutcome
import com.thebrownfoxx.neon.common.type.Jwt

interface TokenStorage {
    suspend fun get(): Outcome<Jwt, GetTokenError>
    suspend fun set(token: Jwt): UnitOutcome<SetTokenError>
    suspend fun delete(): UnitOutcome<SetTokenError>
}