//package com.thebrownfoxx.neon.server.repository.test
//
//import com.thebrownfoxx.neon.common.type.Failure
//import com.thebrownfoxx.neon.common.type.id.GroupId
//import com.thebrownfoxx.neon.common.type.Success
//import com.thebrownfoxx.neon.common.type.UnitSuccess
//import com.thebrownfoxx.neon.common.type.Url
//import com.thebrownfoxx.neon.must.mustBe
//import com.thebrownfoxx.neon.must.mustBeA
//import com.thebrownfoxx.neon.server.model.ChatGroup
//import com.thebrownfoxx.neon.server.model.Community
//import com.thebrownfoxx.neon.server.repository.GroupRepository
//import com.thebrownfoxx.neon.server.repository.group.model.RepositoryAddGroupError
//import com.thebrownfoxx.neon.server.repository.group.model.RepositoryGetGroupError
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.test.runTest
//import kotlin.test.BeforeTest
//import kotlin.test.Test
//
//abstract class GroupRepositoryTest {
//    // TODO: Test the flows for changes
//
//    private val initialGroups = listOf(
//        Community(
//            name = "Formula 1",
//            avatarUrl = Url("https://example.com/f1.png"),
//            isGod = true,
//        ),
//        Community(
//            name = "Formula 2",
//            avatarUrl = Url("https://example.com/f2.png"),
//            isGod = false,
//        ),
//    )
//
//    private lateinit var groupRepository: GroupRepository
//
//    abstract fun createGroupRepository(): GroupRepository
//
//    @BeforeTest
//    fun setup() {
//        runTest {
//            groupRepository = createGroupRepository()
//
//            for (initialGroup in initialGroups) {
//                groupRepository.add(initialGroup)
//            }
//        }
//    }
//
//    @Test
//    fun getShouldReturnGroup() {
//        runTest {
//            for (expectedGroup in initialGroups) {
//                val actualGroupOutcome = groupRepository.get(expectedGroup.id).first()
//                actualGroupOutcome mustBe Success(expectedGroup)
//            }
//        }
//    }
//
//    @Test
//    fun getShouldReturnNotFoundIfGroupDoesNotExist() {
//        runTest {
//            val actualGroupOutcome = groupRepository.get(GroupId()).first()
//            actualGroupOutcome mustBe Failure(RepositoryGetGroupError.NotFound)
//        }
//    }
//
//    @Test
//    fun addShouldAddGroup() {
//        runTest {
//            val expectedGroup = Community(
//                name = "Formula 3",
//                avatarUrl = Url("https://example.com/f3.png"),
//                isGod = false,
//            )
//
//            val addOutcome = groupRepository.add(expectedGroup)
//            addOutcome.mustBeA<UnitSuccess>()
//
//            val actualGroupOutcome = groupRepository.get(expectedGroup.id).first()
//            actualGroupOutcome mustBe Success(expectedGroup)
//        }
//    }
//
//    @Test
//    fun addShouldReturnDuplicateIdIfGroupIdAlreadyExists() {
//        runTest {
//            val duplicateGroup = ChatGroup(id = initialGroups[0].id)
//
//            val actualAddOutcome = groupRepository.add(duplicateGroup)
//            actualAddOutcome mustBe Failure(RepositoryAddGroupError.DuplicateId)
//        }
//    }
//}