package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import com.thebrownfoxx.neon.client.repository.message.MessageRepository
import com.thebrownfoxx.neon.client.repository.test.MessageRepositoryTest

@Suppress("unused")
class InMemoryMessageRepositoryTest : MessageRepositoryTest() {
    override fun createRepositories(): Pair<GroupRepository, MessageRepository> {
        val groupRepository = InMemoryGroupRepository()
        val messageRepository = InMemoryMessageRepository(groupRepository)
        return groupRepository to messageRepository
    }
}