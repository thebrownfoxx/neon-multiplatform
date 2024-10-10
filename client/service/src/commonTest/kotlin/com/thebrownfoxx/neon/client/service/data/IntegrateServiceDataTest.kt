package com.thebrownfoxx.neon.client.service.data

import com.thebrownfoxx.neon.client.repository.memory.InMemoryGroupRepository
import com.thebrownfoxx.neon.client.repository.memory.InMemoryMemberRepository
import com.thebrownfoxx.neon.client.repository.memory.InMemoryMessageRepository
import com.thebrownfoxx.neon.client.repository.memory.InMemoryPasswordRepository
import com.thebrownfoxx.neon.client.service.group.GroupManager
import com.thebrownfoxx.neon.client.service.member.MemberManager
import com.thebrownfoxx.neon.client.service.messenger.Messenger
import com.thebrownfoxx.neon.common.annotation.TestApi
import com.thebrownfoxx.neon.common.hash.MultiplatformHasher
import com.thebrownfoxx.neon.common.model.ChatGroup
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.Delivery
import com.thebrownfoxx.neon.common.model.GroupId
import com.thebrownfoxx.neon.common.model.Member
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Message
import com.thebrownfoxx.neon.common.model.get
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.must.contentMustEqual
import com.thebrownfoxx.neon.must.mustBe
import com.thebrownfoxx.neon.must.mustBeTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(TestApi::class)
class IntegrateServiceDataTest {
    private lateinit var lando: MemberId
    private lateinit var carlos: MemberId
    private lateinit var charles: MemberId
    private lateinit var oscar: MemberId
    private lateinit var andrea: MemberId

    private lateinit var f1: GroupId
    private lateinit var mclaren: GroupId
    private lateinit var directConversation: GroupId

    private val serviceData = serviceData {
        f1 = community(
            name = "Formula 1 Drivers",
            avatarUrl = Url("https://example.com/formula1.jpg"),
            inviteCode = "f1",
        ) {
            lando = member(
                username = "lando_norris",
                avatarUrl = Url("https://example.com/lando.jpg"),
                password = "carlos sainz",
            )

            carlos = member(
                username = "carlos_sainz",
                avatarUrl = Url("https://example.com/carlos.jpg"),
                password = "lando norris",
            )

            charles = member(
                username = "charles_leclerc",
                avatarUrl = Url("https://example.com/charles.jpg"),
                password = "carlos sainz",
            )

            oscar = member(
                username = "oscar_piastri",
                avatarUrl = Url("https://example.com/oscar.jpg"),
                password = "lando norris",
            )
        }.apply {
            conversation {
                lando.said("wtf charles?", Instant.fromEpochSeconds(1))
                charles.said("I'm sorry, Lando", Instant.fromEpochSeconds(2))
                carlos.said("im sorry lando. you know im all yours.", Instant.fromEpochSeconds(3))
            }
        }

        mclaren = community(
            name = "McLaren",
            avatarUrl = Url("https://example.com/mclaren.jpg"),
            inviteCode = "mclaren",
        ) {
            member(lando)
            member(oscar)
            andrea = member(
                username = "andrea_stella",
                avatarUrl = Url("https://example.com/andrea.jpg"),
                password = "toesucker6969",
            )
        }.apply {
            conversation {
                oscar.said("I'm sorry, Lando", Instant.fromEpochSeconds(4))
                lando.said(
                    "not cool. first, charles kissed carlos, then you fucked up my race? im having the worst day ever.",
                    Instant.fromEpochSeconds(5),
                )
                andrea.said("ill suck yalls toes if you stop fighting", Instant.fromEpochSeconds(6))
            }
        }

        directConversation = conversation {
            carlos.said("hey. im in your bed rn 😉", Instant.fromEpochSeconds(7))
            carlos.said("i lost my clothes 😉", Instant.fromEpochSeconds(8))
            carlos.said("can you cover me up with your body?", Instant.fromEpochSeconds(9))
            lando.said("be right there 🤤", Instant.fromEpochSeconds(10))
        }
    }

    private val groupRepository = InMemoryGroupRepository()
    private val memberRepository = InMemoryMemberRepository()
    private val passwordRepository = InMemoryPasswordRepository()
    private val messageRepository = InMemoryMessageRepository(groupRepository)

    private val groupManager: GroupManager = TODO("Add working GroupManager")
    private val memberManager: MemberManager = TODO("Add working MemberManager")
    private val messenger: Messenger = TODO("Add working Messenger")

    @BeforeTest
    fun setup() = runTest {
        serviceData.integrate(groupManager, memberManager, messenger)
    }

    @Test
    fun shouldIntegrateGroupsProperly() = runTest {
        val expectedGroups = listOf(
            Community(
                name = "Formula 1 Drivers",
                avatarUrl = Url("https://example.com/formula1.jpg"),
                inviteCode = "f1",
            ).ignoreId(),
            Community(
                name = "McLaren",
                avatarUrl = Url("https://example.com/mclaren.jpg"),
                inviteCode = "mclaren",
            ).ignoreId(),
            ChatGroup().ignoreId(),
        )

        val actualGroups = groupRepository.groupList.map { it.ignoreId() }
        actualGroups contentMustEqual expectedGroups
    }

    @Test
    fun shouldIntegrateGroupMembersProperly() = runTest {
        val f1ActualMembers = groupRepository.getMembers(f1).first().get()
        f1ActualMembers contentMustEqual setOf(lando, carlos, charles, oscar)

        val mclarenActualMembers = groupRepository.getMembers(mclaren).first().get()
        mclarenActualMembers contentMustEqual setOf(oscar, lando)

        val directConversationActualMembers = groupRepository.getMembers(directConversation).first().get()
        directConversationActualMembers contentMustEqual setOf(carlos, lando)
    }

    @Test
    fun shouldIntegrateMembersProperly() = runTest {
        val expectedMembers = listOf(
            Member(
                username = "lando_norris",
                avatarUrl = Url("https://example.com/lando.jpg"),
            ).ignoreId(),
            Member(
                username = "carlos_sainz",
                avatarUrl = Url("https://example.com/carlos.jpg"),
            ).ignoreId(),
            Member(
                username = "charles_leclerc",
                avatarUrl = Url("https://example.com/charles.jpg"),
            ).ignoreId(),
            Member(
                username = "oscar_piastri",
                avatarUrl = Url("https://example.com/oscar.jpg"),
            ).ignoreId(),
            Member(
                username = "andrea_stella",
                avatarUrl = Url("https://example.com/andrea.jpg"),
            ).ignoreId(),
        )

        val actualMembers = memberRepository.memberList.map { it.ignoreId() }
        actualMembers contentMustEqual expectedMembers
    }

    @Test
    fun shouldIntegratePasswordsProperly() = runTest {
        val expectedPasswords = listOf(
            lando to "carlos sainz",
            carlos to "lando norris",
            charles to "carlos sainz",
            oscar to "lando norris",
            andrea to "toesucker6969",
        )

        val actualPasswords = passwordRepository.passwordHashList

        actualPasswords.size mustBe expectedPasswords.size

        for (index in expectedPasswords.indices) {
            val (expectedMemberId, expectedPassword) = expectedPasswords[index]
            val (actualMemberId, actualPassword) = actualPasswords[index]

            expectedMemberId mustBe actualMemberId
            with(MultiplatformHasher()) {
                (expectedPassword matches actualPassword).mustBeTrue()
            }
        }
    }

    @Test
    fun shouldIntegrateMessagesProperly() = runTest {
        val expectedMessages = listOf(
            Message(
                groupId = f1,
                senderId = lando,
                content = "wtf charles?",
                timestamp = Instant.fromEpochSeconds(1),
                delivery = Delivery.Read,
            ),
            Message(
                groupId = f1,
                senderId = charles,
                content = "I'm sorry, Lando",
                timestamp = Instant.fromEpochSeconds(2),
                delivery = Delivery.Read,
            ),
            Message(
                groupId = f1,
                senderId = carlos,
                content = "im sorry lando. you know im all yours.",
                timestamp = Instant.fromEpochSeconds(3),
                delivery = Delivery.Read,
            ),
            Message(
                groupId = mclaren,
                senderId = oscar,
                content = "I'm sorry, Lando",
                timestamp = Instant.fromEpochSeconds(4),
                delivery = Delivery.Read,
            ),
            Message(
                groupId = mclaren,
                senderId = lando,
                content = "not cool. first, charles kissed carlos, then you fucked up my race? im having the worst day ever.",
                timestamp = Instant.fromEpochSeconds(5),
                delivery = Delivery.Read,
            ),
            Message(
                groupId = mclaren,
                senderId = andrea,
                content = "ill suck yalls toes if you stop fighting",
                timestamp = Instant.fromEpochSeconds(6),
                delivery = Delivery.Read,
            ),
            Message(
                groupId = directConversation,
                senderId = carlos,
                content = "hey. im in your bed rn 😉",
                timestamp = Instant.fromEpochSeconds(7),
                delivery = Delivery.Read,
            ),
            Message(
                groupId = directConversation,
                senderId = carlos,
                content = "i lost my clothes 😉",
                timestamp = Instant.fromEpochSeconds(8),
                delivery = Delivery.Read,
            ),
            Message(
                groupId = directConversation,
                senderId = carlos,
                content = "can you cover me up with your body?",
                timestamp = Instant.fromEpochSeconds(9),
                delivery = Delivery.Read,
            ),
        )

        val actualMessages = messageRepository.messageList.map { it.ignoreId() }

        actualMessages contentMustEqual expectedMessages
    }
}