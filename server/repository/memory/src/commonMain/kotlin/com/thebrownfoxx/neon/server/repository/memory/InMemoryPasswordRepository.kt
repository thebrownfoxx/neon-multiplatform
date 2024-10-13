package com.thebrownfoxx.neon.server.repository.memory

import com.thebrownfoxx.neon.common.annotation.TestApi
import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.common.model.unitSuccess
import com.thebrownfoxx.neon.server.repository.password.PasswordRepository
import com.thebrownfoxx.neon.server.repository.password.model.GetPasswordHashEntityError
import com.thebrownfoxx.neon.server.repository.password.model.SetPasswordHashEntityError

class InMemoryPasswordRepository : PasswordRepository {
    private val passwordHashes = mutableMapOf<MemberId, Hash>()

    @TestApi
    val passwordHashList = passwordHashes.toList()

    override suspend fun getHash(memberId: MemberId): Result<Hash, GetPasswordHashEntityError> {
        val result = when (val passwordHash = passwordHashes[memberId]) {
            null -> Failure(GetPasswordHashEntityError.NotFound)
            else -> Success(passwordHash)
        }

        return result
    }

    override suspend fun setHash(memberId: MemberId, hash: Hash): UnitResult<SetPasswordHashEntityError> {
        passwordHashes[memberId] = hash
        return unitSuccess()
    }
}