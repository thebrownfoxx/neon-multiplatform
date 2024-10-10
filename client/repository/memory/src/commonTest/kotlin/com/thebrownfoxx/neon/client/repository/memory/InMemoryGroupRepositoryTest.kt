package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import com.thebrownfoxx.neon.client.repository.test.GroupRepositoryTest

@Suppress("unused")
class InMemoryGroupRepositoryTest : GroupRepositoryTest() {
    override fun createGroupRepository(): GroupRepository {
        return InMemoryGroupRepository()
    }
}