package com.thebrownfoxx.neon.server.repository.password

import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.UnitOutcome
import com.thebrownfoxx.neon.server.repository.password.model.RepositoryGetPasswordHashError
import com.thebrownfoxx.neon.server.repository.password.model.RepositorySetPasswordHashError

interface PasswordRepository {
    suspend fun getHash(memberId: MemberId): Outcome<Hash, RepositoryGetPasswordHashError>
    suspend fun setHash(memberId: MemberId, hash: Hash): UnitOutcome<RepositorySetPasswordHashError>
}