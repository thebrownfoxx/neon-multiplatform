package com.thebrownfoxx.neon.client.repository.password

import com.thebrownfoxx.neon.client.repository.password.model.SetPasswordHashEntityError
import com.thebrownfoxx.neon.client.repository.password.model.GetPasswordHashEntityError
import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult

interface PasswordRepository {
    suspend fun getHash(memberId: MemberId): Result<Hash, GetPasswordHashEntityError>
    suspend fun setHash(memberId: MemberId, hash: Hash): UnitResult<SetPasswordHashEntityError>
}