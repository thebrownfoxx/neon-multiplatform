package com.thebrownfoxx.neon.server.repository.password

import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.password.model.RepositoryGetPasswordHashError
import com.thebrownfoxx.neon.server.repository.password.model.RepositorySetPasswordHashError

interface PasswordRepository {
    suspend fun getHash(memberId: MemberId): Result<Hash, RepositoryGetPasswordHashError>
    suspend fun setHash(memberId: MemberId, hash: Hash): UnitResult<RepositorySetPasswordHashError>
}