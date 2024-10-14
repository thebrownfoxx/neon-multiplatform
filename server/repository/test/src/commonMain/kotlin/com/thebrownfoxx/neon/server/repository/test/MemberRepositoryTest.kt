package com.thebrownfoxx.neon.server.repository.test

import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.UnitSuccess
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.must.mustBe
import com.thebrownfoxx.neon.must.mustBeA
import com.thebrownfoxx.neon.server.repository.member.MemberRepository
import com.thebrownfoxx.neon.server.repository.member.model.AddMemberEntityError
import com.thebrownfoxx.neon.server.repository.member.model.GetMemberEntityError
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

abstract class MemberRepositoryTest {
    private val members = listOf(
        Member(
            username = "lando_norris",
            avatarUrl = Url("https://example.com/lando_norris.png"),
        ),
        Member(
            username = "lewis_hamilton",
            avatarUrl = Url("https://example.com/lewis_hamilton.png"),
        ),
    )

    private lateinit var memberRepository: MemberRepository

    abstract fun createMemberRepository(): MemberRepository

    @BeforeTest
    fun setup() {
        runTest {
            memberRepository = createMemberRepository()

            for (member in members) {
                memberRepository.add(member)
            }
        }
    }

    @Test
    fun getShouldGetMember() {
        runTest {
            for (expectedMember in members) {
                val actualMemberResult = memberRepository.get(expectedMember.id).first()
                actualMemberResult mustBe Success(expectedMember)
            }
        }
    }

    @Test
    fun getShouldReturnNotFoundIfMemberDoesNotExist() {
        runTest {
            val actualMemberResult = memberRepository.get(MemberId()).first()
            actualMemberResult mustBe Failure(GetMemberEntityError.NotFound)
        }
    }

    @Test
    fun addShouldAddMember() {
        runTest {
            val expectedMember = Member(
                username = "charles_leclerc",
                avatarUrl = Url("https://example.com/charles_leclerc.png"),
            )

            val addResult = memberRepository.add(expectedMember)
            addResult.mustBeA<UnitSuccess>()

            val actualMemberResult = memberRepository.get(expectedMember.id).first()
            actualMemberResult mustBe Success(expectedMember)
        }
    }

    @Test
    fun addShouldReturnDuplicateIdIfMemberIdAlreadyExists() {
        runTest {
            val duplicateMember = Member(
                id = members[0].id,
                username = "charles_leclerc",
                avatarUrl = Url("https://example.com/charles_leclerc.png"),
            )

            val actualAddResult = memberRepository.add(duplicateMember)
            actualAddResult mustBe Failure(AddMemberEntityError.DuplicateId)
        }
    }

    @Test
    fun addShouldReturnDuplicateUsernameIfUsernameAlreadyExists() {
        runTest {
            val duplicateMember = Member(
                username = members[0].username,
                avatarUrl = Url("https://example.com/charles_leclerc.png"),
            )

            val actualAddResult = memberRepository.add(duplicateMember)
            actualAddResult mustBe Failure(AddMemberEntityError.DuplicateUsername)
        }
    }
}