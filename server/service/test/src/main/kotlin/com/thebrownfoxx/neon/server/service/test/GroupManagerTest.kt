package com.thebrownfoxx.neon.server.service.test

import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.server.service.group.GroupManager
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest

abstract class GroupManagerTest {
    private val initialCommunities = listOf(
        Community(
            name = "The Community",
            avatarUrl = null,
            god = true,
        ).ignoreId(),
        Community(
            name = "The Other Community",
            avatarUrl = null,
            god = false,
        ).ignoreId(),
    )

    private lateinit var groupManager: GroupManager

    abstract fun createGroupManager(): GroupManager

    @BeforeTest
    fun setup() = runTest {
        groupManager = createGroupManager()
        for (community in initialCommunities) {
            groupManager.createCommunity(
                name = community.name,
                god = community.god,
            )
        }
    }

    // TODO: Figure out if writing the tests is even worth it
}