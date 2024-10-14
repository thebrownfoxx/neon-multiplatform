package com.thebrownfoxx.neon.server.repository.memory

import com.thebrownfoxx.neon.server.repository.test.MessageRepositoryTest
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.message.MessageRepository

@Suppress("unused")
class InMemoryMessageRepositoryTest : MessageRepositoryTest() {
    override fun createRepositories(): Pair<GroupRepository, MessageRepository> {
        val groupRepository = InMemoryGroupRepository()
        val messageRepository = InMemoryMessageRepository(groupRepository)
        return groupRepository to messageRepository
    }
}