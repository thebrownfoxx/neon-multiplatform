package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.hash.Hash
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.id.MemberId

interface PasswordRepository {
    suspend fun getHash(memberId: MemberId): Outcome<Hash, GetError>
    suspend fun setHash(memberId: MemberId, hash: Hash): ReversibleUnitOutcome<ConnectionError>
}