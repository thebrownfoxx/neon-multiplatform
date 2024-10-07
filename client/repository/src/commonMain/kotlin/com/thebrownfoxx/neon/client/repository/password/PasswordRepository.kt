package com.thebrownfoxx.neon.client.repository.password

import com.thebrownfoxx.neon.client.repository.password.model.SetPasswordError
import com.thebrownfoxx.neon.client.repository.password.model.GetPasswordHashError
import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result
import com.thebrownfoxx.neon.common.model.UnitResult

interface PasswordRepository {
    suspend fun getHash(memberId: MemberId): Result<Hash, GetPasswordHashError>
    suspend fun set(memberId: MemberId, hash: Hash): UnitResult<SetPasswordError>
}