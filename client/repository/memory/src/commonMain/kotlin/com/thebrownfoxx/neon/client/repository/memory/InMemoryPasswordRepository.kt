package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.password.PasswordRepository
import com.thebrownfoxx.neon.client.repository.password.model.GetPasswordHashError
import com.thebrownfoxx.neon.client.repository.password.model.SetPasswordError
import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.common.model.unitSuccess

class InMemoryPasswordRepository : PasswordRepository {
    private val passwordHashes = mutableMapOf<MemberId, Hash>()

    override suspend fun getHash(memberId: MemberId): Result<Hash, GetPasswordHashError> {
        val result = when (val passwordHash = passwordHashes[memberId]) {
            null -> Failure(GetPasswordHashError.NotFound)
            else -> Success(passwordHash)
        }

        return result
    }

    override suspend fun set(memberId: MemberId, hash: Hash): UnitResult<SetPasswordError> {
        passwordHashes[memberId] = hash
        return unitSuccess()
    }
}