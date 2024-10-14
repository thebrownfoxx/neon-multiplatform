package com.thebrownfoxx.neon.server.repository.test

import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.unitSuccess
import com.thebrownfoxx.neon.must.mustBe
import com.thebrownfoxx.neon.server.repository.invite.InviteCodeRepository
import com.thebrownfoxx.neon.server.repository.invite.model.GetInviteCodeError
import com.thebrownfoxx.neon.server.repository.invite.model.GetInviteCodeGroupError
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

abstract class InviteCodeRepositoryTest {
    private val initialInviteCodes = listOf(
        InviteCode(GroupId(), "f1"),
        InviteCode(GroupId(), "f2"),
        InviteCode(GroupId(), "f3"),
    )

    private lateinit var inviteCodeRepository: InviteCodeRepository

    abstract fun createInviteCodeRepository(): InviteCodeRepository

    @BeforeTest
    fun setup() = runTest {
        inviteCodeRepository = createInviteCodeRepository()

        for ((groupId, inviteCode) in initialInviteCodes) {
            inviteCodeRepository.set(groupId, inviteCode)
        }
    }

    @Test
    fun getShouldReturnInviteCode() = runTest {
        for ((groupId, inviteCode) in initialInviteCodes) {
            val actualInviteCode = inviteCodeRepository.get(groupId)
            actualInviteCode mustBe Success(inviteCode)
        }
    }

    @Test
    fun getShouldReturnNotFoundIfInviteCodeDoesNotExist() = runTest {
        val actualInviteCode = inviteCodeRepository.get(GroupId())
        actualInviteCode mustBe Failure(GetInviteCodeError.NotFound)
    }

    @Test
    fun getGroupShouldReturnGroupId() = runTest {
        for ((groupId, inviteCode) in initialInviteCodes) {
            val actualGroupId = inviteCodeRepository.getGroup(inviteCode)
            actualGroupId mustBe Success(groupId)
        }
    }

    @Test
    fun getGroupShouldReturnNotFoundIfInviteCodeDoesNotExist() = runTest {
        val actualGroupId = inviteCodeRepository.getGroup("invalid")
        actualGroupId mustBe Failure(GetInviteCodeGroupError.NotFound)
    }

    @Test
    fun setShouldSetInviteCode() = runTest {
        val groupId = GroupId()
        val inviteCode = "f4"

        val setResult = inviteCodeRepository.set(groupId, inviteCode)
        setResult mustBe unitSuccess()

        val actualInviteCode = inviteCodeRepository.get(groupId)
        actualInviteCode mustBe Success(inviteCode)
    }
}

private data class InviteCode(
    val groupId: GroupId,
    val value: String,
)