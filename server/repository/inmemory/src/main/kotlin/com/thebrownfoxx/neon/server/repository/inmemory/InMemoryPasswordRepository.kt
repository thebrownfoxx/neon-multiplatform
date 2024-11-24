package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.common.annotation.TestApi
import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.common.type.unitSuccess
import com.thebrownfoxx.neon.server.repository.password.PasswordRepository
import com.thebrownfoxx.neon.server.repository.password.model.RepositoryGetPasswordHashError
import com.thebrownfoxx.neon.server.repository.password.model.RepositorySetPasswordHashError

class InMemoryPasswordRepository : PasswordRepository {
    private val passwordHashes = mutableMapOf<MemberId, Hash>()

    @TestApi
    val passwordHashList = passwordHashes.toList()

    override suspend fun getHash(memberId: MemberId): Outcome<Hash, RepositoryGetPasswordHashError> {
        return when (val passwordHash = passwordHashes[memberId]) {
            null -> Failure(RepositoryGetPasswordHashError.NotFound)
            else -> Success(passwordHash)
        }
    }

    override suspend fun setHash(memberId: MemberId, hash: Hash): UnitOutcome<RepositorySetPasswordHashError> {
        passwordHashes[memberId] = hash
        return unitSuccess()
    }
}