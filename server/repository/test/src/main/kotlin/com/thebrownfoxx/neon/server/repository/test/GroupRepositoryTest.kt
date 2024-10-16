package com.thebrownfoxx.neon.server.repository.test

import com.thebrownfoxx.neon.common.model.ChatGroup
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitSuccess
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.must.mustBe
import com.thebrownfoxx.neon.must.mustBeA
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.group.model.RepositoryAddGroupError
import com.thebrownfoxx.neon.server.repository.group.model.RepositoryGetGroupError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

abstract class GroupRepositoryTest {
    // TODO: Test the flows for changes

    private val initialGroups = listOf(
        Community(
            name = "Formula 1",
            avatarUrl = Url("https://example.com/f1.png"),
            god = true,
        ),
        Community(
            name = "Formula 2",
            avatarUrl = Url("https://example.com/f2.png"),
            god = false,
        ),
    )

    private lateinit var groupRepository: GroupRepository

    abstract fun createGroupRepository(): GroupRepository

    @BeforeTest
    fun setup() {
        runTest {
            groupRepository = createGroupRepository()

            for (initialGroup in initialGroups) {
                groupRepository.add(initialGroup)
            }
        }
    }

    @Test
    fun getShouldReturnGroup() {
        runTest {
            for (expectedGroup in initialGroups) {
                val actualGroupResult = groupRepository.get(expectedGroup.id).first()
                actualGroupResult mustBe Success(expectedGroup)
            }
        }
    }

    @Test
    fun getShouldReturnNotFoundIfGroupDoesNotExist() {
        runTest {
            val actualGroupResult = groupRepository.get(GroupId()).first()
            actualGroupResult mustBe Failure(RepositoryGetGroupError.NotFound)
        }
    }

    @Test
    fun addShouldAddGroup() {
        runTest {
            val expectedGroup = Community(
                name = "Formula 3",
                avatarUrl = Url("https://example.com/f3.png"),
                god = false,
            )

            val addResult = groupRepository.add(expectedGroup)
            addResult.mustBeA<UnitSuccess>()

            val actualGroupResult = groupRepository.get(expectedGroup.id).first()
            actualGroupResult mustBe Success(expectedGroup)
        }
    }

    @Test
    fun addShouldReturnDuplicateIdIfGroupIdAlreadyExists() {
        runTest {
            val duplicateGroup = ChatGroup(id = initialGroups[0].id)

            val actualAddResult = groupRepository.add(duplicateGroup)
            actualAddResult mustBe Failure(RepositoryAddGroupError.DuplicateId)
        }
    }
}