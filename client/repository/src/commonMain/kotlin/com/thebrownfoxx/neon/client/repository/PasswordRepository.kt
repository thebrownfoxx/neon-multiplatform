package com.thebrownfoxx.neon.client.repository

import com.thebrownfoxx.neon.client.repository.model.AddEntityResult
import com.thebrownfoxx.neon.client.repository.model.GetEntityResult
import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.model.MemberId

interface PasswordRepository {
    suspend fun getHash(memberId: MemberId): GetEntityResult<Hash>
    suspend fun add(memberId: MemberId, hash: Hash): AddEntityResult
}