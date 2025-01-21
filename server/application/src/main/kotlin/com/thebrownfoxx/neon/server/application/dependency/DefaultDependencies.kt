package com.thebrownfoxx.neon.server.application.dependency

import com.thebrownfoxx.neon.common.extension.ago
import com.thebrownfoxx.neon.common.hash.MultiplatformHasher
import com.thebrownfoxx.neon.common.logError
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.application.environment.DotEnvironment
import com.thebrownfoxx.neon.server.application.environment.ServerEnvironmentKey.JwtSecret
import com.thebrownfoxx.neon.server.application.environment.ServerEnvironmentKey.PostgresPassword
import com.thebrownfoxx.neon.server.application.websocket.WebSocketManager
import com.thebrownfoxx.neon.server.model.Delivery
import com.thebrownfoxx.neon.server.repository.data.integrate
import com.thebrownfoxx.neon.server.repository.data.serviceData
import com.thebrownfoxx.neon.server.repository.exposed.ExposedConfigurationRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedGroupMemberRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedGroupRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedInviteCodeRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedMemberRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedMessageRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedPasswordRepository
import com.thebrownfoxx.neon.server.service.JwtConfig
import com.thebrownfoxx.neon.server.service.default.DefaultAuthenticator
import com.thebrownfoxx.neon.server.service.default.DefaultGroupManager
import com.thebrownfoxx.neon.server.service.default.DefaultJwtProcessor
import com.thebrownfoxx.neon.server.service.default.DefaultMemberManager
import com.thebrownfoxx.neon.server.service.default.DefaultMessenger
import com.thebrownfoxx.neon.server.service.default.DefaultPermissionChecker
import com.thebrownfoxx.outcome.map.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class DefaultDependencies : Dependencies {
    override val environment = DotEnvironment()

    override val applicationScope = CoroutineScope(SupervisorJob())

    override val webSocketManager = WebSocketManager()

    private val jwtConfig = JwtConfig(
        realm = "neon",
        issuer = "https://neon/",
        audience = "neon-audience",
        validity = 30.days,
        secret = environment[JwtSecret],
    )

    override val jwtProcessor = DefaultJwtProcessor(jwtConfig)

    private val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/neon",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = environment[PostgresPassword],
    )

    private val configurationRepository = ExposedConfigurationRepository(database)
    private val groupRepository = ExposedGroupRepository(database, applicationScope)
    private val memberRepository = ExposedMemberRepository(database, applicationScope)
    private val groupMemberRepository = ExposedGroupMemberRepository(database, applicationScope)
    private val inviteCodeRepository = ExposedInviteCodeRepository(database, applicationScope)
    private val passwordRepository = ExposedPasswordRepository(database)
    private val messageRepository = ExposedMessageRepository(database, applicationScope)
    private val hasher = MultiplatformHasher()
    private val permissionChecker = DefaultPermissionChecker(groupMemberRepository)

    override val authenticator = DefaultAuthenticator(memberRepository, passwordRepository, hasher)

    override val groupManager = DefaultGroupManager(
        permissionChecker,
        groupRepository,
        memberRepository,
        groupMemberRepository,
        inviteCodeRepository,
    )

    override val memberManager = DefaultMemberManager(
        memberRepository,
        passwordRepository,
        inviteCodeRepository,
        groupMemberRepository,
        hasher,
    )

    override val messenger = DefaultMessenger(
        messageRepository,
        memberRepository,
        groupRepository,
        groupMemberRepository,
    )

    init {
        runBlocking {
            generateInitialServiceData().integrate(
                configurationRepository,
                groupRepository,
                memberRepository,
                groupMemberRepository,
                inviteCodeRepository,
                passwordRepository,
                messageRepository,
                hasher,
            ).onFailure { logError() }
        }
    }
}

private fun generateInitialServiceData() = serviceData {
    lateinit var lando: MemberId
    lateinit var carlos: MemberId
    lateinit var charles: MemberId
    lateinit var oscar: MemberId
    lateinit var andrea: MemberId

    community(
        name = "Formula 1 Drivers",
        avatarUrl = Url("https://upload.wikimedia.org/wikipedia/commons/b/bc/F1logonew.jpg"),
        inviteCode = "f1",
        isGod = true,
    ) {
        lando = member(
            username = "lando_norris",
            avatarUrl = Url("https://lh3.googleusercontent.com/pw/AP1GczMMaGD6saUvy-J2Mhwz4_pvHCLIwRLB_oCzDlCaWiOwec3513sq-HRE9p4tX7Y-Pt83M1zMU16orKnfgIo8HcB_Xcec2nSX8nsDN8yUUj3quvPzMgMPVDQXl2tev7GP6S8pqltC5jssZnViPdpoCXnsGg=w558-h558-s-no-gm"),
            password = "carlos sainz",
        )

        carlos = member(
            username = "carlos_sainz",
            avatarUrl = Url("https://lh3.googleusercontent.com/pw/AP1GczNk1Jhj1lh1PPxfbLcbhzjdPC_dPiApSkiSjeCQpQx-Zj_2pHD5EC1NqotoaEHcG4CK5YaC5lHo9bSo6rSdZDcSjXhV0tpwGAaOjruBtD2nNZkVPqpg26JSNI2hXqhsd1bVFxiNgPKhseAPZiyNQRfr1A=w536-h536-s-no-gm"),
            password = "lando norris",
        )

        charles = member(
            username = "charles_leclerc",
            avatarUrl = Url("https://lh3.googleusercontent.com/pw/AP1GczN6U_gNfBrdLkfr-IVxg-9CHjgfizYBvYgzkmvr6jOF9HpKn1sEAzddTrWGrRSB8_wOsZkwQs_Lj2Q22M2jqCLZHLjaxyCgqF951y_rYMLU55hDhDLiEzFkmFigSJC_HUlICyd2WH_wRIy0_g8BdoHdOA=w608-h609-s-no-gm"),
            password = "carlos sainz",
        )

        oscar = member(
            username = "oscar_piastri",
            avatarUrl = Url("https://upload.wikimedia.org/wikipedia/commons/thumb/4/49/Koala_climbing_tree.jpg/640px-Koala_climbing_tree.jpg"),
            password = "lando norris",
        )
    }.conversation {
        lando.said("wtf charles?", 10.minutes.ago)
        charles.said("I'm sorry, Lando", 8.minutes.ago)
        carlos.said("im sorry lando. you know im all yours.", 7.minutes.ago, Delivery.Sent)
    }

    community(
        name = "McLaren",
        avatarUrl = Url("https://static.wikia.nocookie.net/f1-formula-1/images/e/ed/McLaren.jpg/revision/latest?cb=20230118201145"),
        inviteCode = "mclaren",
    ) {
        member(lando)
        member(oscar)
        andrea = member(
            username = "andrea_stella",
            avatarUrl = null,
            password = "toesucker6969",
        )
    }.conversation {
        oscar.said("I'm sorry, Lando", 8.minutes.ago)
        lando.said(
            "not cool. first, charles kissed carlos, then you fucked up my race? im having the worst day ever.",
            6.minutes.ago,
        )
        andrea.said(
            "ill suck yalls toes if you stop fighting",
            5.minutes.ago,
            Delivery.Delivered,
        )
    }

    conversation {
        carlos.said("hey. im in your bed rn 😉", 3.minutes.ago)
        carlos.said("i lost my clothes 😉", 3.minutes.ago)
        carlos.said("can you cover me up with your body?", 2.minutes.ago)
        lando.said("be right there 🤤", 2.minutes.ago)
    }
}