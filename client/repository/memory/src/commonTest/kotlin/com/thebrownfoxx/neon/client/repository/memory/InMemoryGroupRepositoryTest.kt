package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import kotlin.test.BeforeTest

class InMemoryGroupRepositoryTest : GroupRepository by InMemoryGroupRepository() {

    @BeforeTest
    fun setup() {
    }
}