package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.PasswordRepository
import com.thebrownfoxx.neon.client.repository.model.AddEntityError
import com.thebrownfoxx.neon.client.repository.model.AddEntityResult
import com.thebrownfoxx.neon.client.repository.model.GetEntityError
import com.thebrownfoxx.neon.client.repository.model.GetEntityResult
import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitSuccess

class InMemoryPasswordRepository : PasswordRepository {
    private val passwordHashes = mutableMapOf<MemberId, Hash>()

    override suspend fun getHash(memberId: MemberId): GetEntityResult<Hash> {
        val result = when (val passwordHash = passwordHashes[memberId]) {
            null -> Failure(GetEntityError.NotFound)
            else -> Success(passwordHash)
        }

        return result
    }

    override suspend fun add(memberId: MemberId, hash: Hash): AddEntityResult {
        return when {
            passwordHashes.containsKey(memberId) -> Failure(AddEntityError.DuplicateId)
            else -> {
                passwordHashes[memberId] = hash
                UnitSuccess()
            }
        }
    }
}