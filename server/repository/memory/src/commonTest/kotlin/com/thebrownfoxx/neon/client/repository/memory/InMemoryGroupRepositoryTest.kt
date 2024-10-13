package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.server.repository.test.GroupRepositoryTest
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.memory.InMemoryGroupRepository

@Suppress("unused")
class InMemoryGroupRepositoryTest : GroupRepositoryTest() {
    override fun createGroupRepository(): GroupRepository {
        return InMemoryGroupRepository()
    }
}