package com.thebrownfoxx.neon.server.repository.inmemory

import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.test.GroupRepositoryTest

@Suppress("unused")
class InMemoryGroupRepositoryTest : GroupRepositoryTest() {
    override fun createGroupRepository(): GroupRepository {
        return InMemoryGroupRepository()
    }
}