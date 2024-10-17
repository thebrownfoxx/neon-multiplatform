package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.server.repository.groupmember.GroupMemberRepository
import com.thebrownfoxx.neon.server.repository.message.MessageRepository
import com.thebrownfoxx.neon.server.repository.test.MessageRepositoryTest

@Suppress("unused")
class InMemoryMessageRepositoryTest : MessageRepositoryTest() {
    override fun createRepositories(): Pair<GroupMemberRepository, MessageRepository> {
        val groupRepository = TODO()
        val messageRepository = InMemoryMessageRepository(groupRepository)
        return groupRepository to messageRepository
    }
}