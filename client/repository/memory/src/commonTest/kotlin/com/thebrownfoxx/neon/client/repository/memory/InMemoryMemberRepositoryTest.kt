package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.member.MemberRepository
import com.thebrownfoxx.neon.client.repository.test.MemberRepositoryTest

@Suppress("unused")
class InMemoryMemberRepositoryTest : MemberRepositoryTest() {
    override fun createMemberRepository(): MemberRepository {
        return InMemoryMemberRepository()
    }
}