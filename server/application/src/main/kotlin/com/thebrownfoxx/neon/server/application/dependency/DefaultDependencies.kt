package com.thebrownfoxx.neon.server.application.dependency

import com.thebrownfoxx.neon.common.hash.MultiplatformHasher
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.neon.server.application.websocket.WebSocketManager
import com.thebrownfoxx.neon.server.repository.data.integrate
import com.thebrownfoxx.neon.server.repository.data.serviceData
import com.thebrownfoxx.neon.server.repository.exposed.ExposedConfigurationRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedGroupMemberRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedGroupRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedInviteCodeRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedMemberRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedMessageRepository
import com.thebrownfoxx.neon.server.repository.exposed.ExposedPasswordRepository
import com.thebrownfoxx.neon.server.service.default.DefaultAuthenticator
import com.thebrownfoxx.neon.server.service.default.DefaultGroupManager
import com.thebrownfoxx.neon.server.service.default.DefaultJwtProcessor
import com.thebrownfoxx.neon.server.service.default.DefaultMemberManager
import com.thebrownfoxx.neon.server.service.default.DefaultMessenger
import com.thebrownfoxx.neon.server.service.default.DefaultPermissionChecker
import com.thebrownfoxx.neon.server.service.jwt.model.JwtConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.Database
import kotlin.time.Duration.Companion.days

class DefaultDependencies : Dependencies {
    override val webSocketManager = WebSocketManager()

    private val jwtConfig =
        JwtConfig(
            realm = "neon",
            issuer = "https://neon/",
            audience = "neon-audience",
            validity = 30.days,
            secret = "secret", // TODO: This should be moved to env
        )

    override val jwtProcessor = DefaultJwtProcessor(jwtConfig)

    private val database = Database.connect(
        url = "jdbc:postgresql://localhost:5432/neon",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "development", // TODO: Move to env
    )

    private val configurationRepository = ExposedConfigurationRepository(database)
    private val groupRepository = ExposedGroupRepository(database)
    private val memberRepository = ExposedMemberRepository(database)
    private val groupMemberRepository = ExposedGroupMemberRepository(database)
    private val inviteCodeRepository = ExposedInviteCodeRepository(database)
    private val passwordRepository = ExposedPasswordRepository(database)
    private val messageRepository = ExposedMessageRepository(database)
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
        CoroutineScope(Dispatchers.IO).launch {
            generateInitialServiceData().integrate(
                configurationRepository,
                groupRepository,
                memberRepository,
                groupMemberRepository,
                inviteCodeRepository,
                passwordRepository,
                messageRepository,
                hasher,
            )
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
        avatarUrl = Url("https://example.com/formula1.jpg"),
        inviteCode = "f1",
        isGod = true,
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
            carlos.said(
                "im sorry lando. you know im all yours.",
                Instant.fromEpochSeconds(3),
            )
        }
    }

    community(
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
            andrea.said(
                "ill suck yalls toes if you stop fighting",
                Instant.fromEpochSeconds(6),
            )
        }
    }

    conversation {
        carlos.said("hey. im in your bed rn 😉", Instant.fromEpochSeconds(7))
        carlos.said("i lost my clothes 😉", Instant.fromEpochSeconds(8))
        carlos.said(
            "can you cover me up with your body?",
            Instant.fromEpochSeconds(9)
        )
        lando.said("be right there 🤤", Instant.fromEpochSeconds(10))
    }
}