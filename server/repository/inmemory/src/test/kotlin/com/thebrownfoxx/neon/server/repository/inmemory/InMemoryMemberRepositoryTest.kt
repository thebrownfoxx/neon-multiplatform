package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.test.MemberRepositoryTest

@Suppress("unused")
class InMemoryMemberRepositoryTest : MemberRepositoryTest() {
    override fun createMemberRepository(): MemberRepository {
        return InMemoryMemberRepository()
    }
}