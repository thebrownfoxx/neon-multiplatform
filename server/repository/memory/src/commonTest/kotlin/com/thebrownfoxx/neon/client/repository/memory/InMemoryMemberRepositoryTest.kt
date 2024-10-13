package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.server.repository.test.MemberRepositoryTest
import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.memory.InMemoryMemberRepository

@Suppress("unused")
class InMemoryMemberRepositoryTest : MemberRepositoryTest() {
    override fun createMemberRepository(): MemberRepository {
        return InMemoryMemberRepository()
    }
}