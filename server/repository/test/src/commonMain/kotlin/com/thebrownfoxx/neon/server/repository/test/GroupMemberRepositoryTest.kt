//package com.thebrownfoxx.neon.server.repository.test
//
//import com.thebrownfoxx.neon.common.type.Success
//import com.thebrownfoxx.neon.common.type.id.GroupId
//import com.thebrownfoxx.neon.common.type.id.MemberId
//import com.thebrownfoxx.neon.common.type.unitSuccess
//import com.thebrownfoxx.neon.must.mustBe
//import com.thebrownfoxx.neon.server.repository.GroupMemberRepository
//import kotlinx.coroutines.flow.first
//import kotlinx.coroutines.test.runTest
//import kotlin.test.BeforeTest
//import kotlin.test.Test
//
//abstract class GroupMemberRepositoryTest {
//    private val groupXId = GroupId()
//    private val memberYId = MemberId()
//
//    private val initialGroupMembers = listOf(
//        GroupMember(groupXId, MemberId(), isAdmin = true),
//        GroupMember(groupXId, memberYId),
//        GroupMember(GroupId(), memberYId, isAdmin = true),
//    )
//
//    private lateinit var groupMemberRepository: GroupMemberRepository
//
//    abstract fun createRepository(): GroupMemberRepository
//
//    @BeforeTest
//    fun setup() = runTest {
//        groupMemberRepository = createRepository()
//
//        for (groupMember in initialGroupMembers) {
//            groupMemberRepository.addMember(groupMember.groupId, groupMember.memberId)
//        }
//    }
//
//    @Test
//    fun getMembersShouldReturnMembers() = runTest {
//        val actualMembers = groupMemberRepository.getMembersAsFlow(groupXId).first()
//
//        val expectedMembers = initialGroupMembers
//            .filter { it.groupId == groupXId }
//            .map { it.memberId }
//        actualMembers mustBe Success(expectedMembers)
//    }
//
//    @Test
//    fun getGroupsShouldReturnGroups() = runTest {
//        val actualGroups = groupMemberRepository.getGroupsAsFlow(memberYId).first()
//
//        val expectedGroups = initialGroupMembers
//            .filter { it.memberId == memberYId }
//            .map { it.groupId }
//        actualGroups mustBe Success(expectedGroups)
//    }
//
//    @Test
//    fun getAdminsShouldReturnAdmins() = runTest {
//        val actualAdmins = groupMemberRepository.getAdminsAsFlow(groupXId).first()
//
//        val expectedAdmins = initialGroupMembers
//            .filter { it.groupId == groupXId && it.isAdmin }
//            .map { it.memberId }
//
//        actualAdmins mustBe Success(expectedAdmins)
//    }
//
//    @Test
//    fun addMemberShouldAddMember() = runTest {
//        val memberId = MemberId()
//
//        val addOutcome = groupMemberRepository.addMember(groupXId, memberId)
//        addOutcome mustBe UnitSuccess
//
//        val actualMembers = groupMemberRepository.getMembersAsFlow(groupXId).first()
//
//        val expectedMembers = initialGroupMembers
//            .filter { it.groupId == groupXId }
//            .map { it.memberId } + memberId
//
//        actualMembers mustBe Success(expectedMembers)
//    }
//}
//
//private data class GroupMember(
//    val groupId: GroupId,
//    val memberId: MemberId,
//    val isAdmin: Boolean = false,
//)