package com.thebrownfoxx.neon.server.repository.password

import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult
import com.thebrownfoxx.neon.server.repository.password.model.GetPasswordHashEntityError
import com.thebrownfoxx.neon.server.repository.password.model.SetPasswordHashEntityError

interface PasswordRepository {
    suspend fun getHash(memberId: MemberId): Result<Hash, GetPasswordHashEntityError>
    suspend fun setHash(memberId: MemberId, hash: Hash): UnitResult<SetPasswordHashEntityError>
}