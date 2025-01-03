package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface TokenRepository {
    fun getAsFlow(): Flow<Outcome<Jwt, GetError>>
    suspend fun get(): Outcome<Jwt, GetError>
    suspend fun set(token: Jwt): UnitOutcome<DataOperationError>
    suspend fun clear(): UnitOutcome<DataOperationError>
}