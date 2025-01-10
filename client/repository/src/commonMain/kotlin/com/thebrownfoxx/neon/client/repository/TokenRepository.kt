package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.Jwt
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.UnitOutcome
import kotlinx.coroutines.flow.Flow

interface TokenRepository {
    fun getAsFlow(): Flow<Outcome<Token, GetError>>
    suspend fun get(): Outcome<Token, GetError>
    suspend fun set(token: Token): UnitOutcome<DataOperationError>
    suspend fun clear(): UnitOutcome<DataOperationError>

    data class Token(val jwt: Jwt, val memberId: MemberId)
}