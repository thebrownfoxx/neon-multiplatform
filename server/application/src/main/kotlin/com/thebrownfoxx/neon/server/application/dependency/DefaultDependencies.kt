package com.thebrownfoxx.neon.server.application.dependency

import com.thebrownfoxx.neon.common.PrintLogger
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
import com.thebrownfoxx.neon.server.service.JwtConfig
import com.thebrownfoxx.neon.server.service.default.DefaultAuthenticator
import com.thebrownfoxx.neon.server.service.default.DefaultGroupManager
import com.thebrownfoxx.neon.server.service.default.DefaultJwtProcessor
import com.thebrownfoxx.neon.server.service.default.DefaultMemberManager
import com.thebrownfoxx.neon.server.service.default.DefaultMessenger
import com.thebrownfoxx.neon.server.service.default.DefaultPermissionChecker
import com.thebrownfoxx.outcome.map.onFailure
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

    override val logger = PrintLogger

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
            ).onFailure { logger.logError(it) }
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

private fun generateExtensiveF1ServiceData() = serviceData {
    lateinit var max: MemberId
    lateinit var checo: MemberId
    lateinit var lewis: MemberId
    lateinit var george: MemberId
    lateinit var charles: MemberId
    lateinit var carlos: MemberId
    lateinit var lando: MemberId
    lateinit var oscar: MemberId
    lateinit var pierre: MemberId
    lateinit var esteban: MemberId
    lateinit var christian: MemberId
    lateinit var toto: MemberId
    lateinit var mattia: MemberId

    community(
        name = "Formula 1 Drivers Global",
        avatarUrl = Url("https://example.com/f1global.jpg"),
        inviteCode = "f1world",
        isGod = true,
    ) {
        max = member(
            username = "max_verstappen",
            avatarUrl = Url("https://example.com/max.jpg"),
            password = "world_champion",
        )

        checo = member(
            username = "checo_perez",
            avatarUrl = Url("https://example.com/checo.jpg"),
            password = "number_two_driver",
        )

        lewis = member(
            username = "lewis_hamilton",
            avatarUrl = Url("https://example.com/lewis.jpg"),
            password = "seven_time_champ",
        )

        george = member(
            username = "george_russell",
            avatarUrl = Url("https://example.com/george.jpg"),
            password = "rising_star",
        )

        charles = member(
            username = "charles_leclerc",
            avatarUrl = Url("https://example.com/charles.jpg"),
            password = "monaco_prince",
        )

        carlos = member(
            username = "carlos_sainz",
            avatarUrl = Url("https://example.com/carlos.jpg"),
            password = "el_matador",
        )

        lando = member(
            username = "lando_norris",
            avatarUrl = Url("https://example.com/lando.jpg"),
            password = "twitch_streamer",
        )

        oscar = member(
            username = "oscar_piastri",
            avatarUrl = Url("https://example.com/oscar.jpg"),
            password = "rookie_sensation",
        )

        pierre = member(
            username = "pierre_gasly",
            avatarUrl = Url("https://example.com/pierre.jpg"),
            password = "french_flair",
        )

        esteban = member(
            username = "esteban_ocon",
            avatarUrl = Url("https://example.com/esteban.jpg"),
            password = "home_hero",
        )

        christian = member(
            username = "christian_horner",
            avatarUrl = Url("https://example.com/horner.jpg"),
            password = "rb_boss",
        )

        toto = member(
            username = "toto_wolff",
            avatarUrl = Url("https://example.com/toto.jpg"),
            password = "mercedes_mastermind",
        )

        mattia = member(
            username = "mattia_binotto",
            avatarUrl = Url("https://example.com/mattia.jpg"),
            password = "ferrari_strategist",
        )
    }.apply {
        conversation {
            // Random messages with earlier timestamps
            repeat(500) { i ->
                val randomMembers = listOf(max, checo, lewis, george, charles, carlos, lando, oscar, pierre, esteban)
                val randomSender = randomMembers.random()
                randomSender.said("Random global chat message ${i+1}", Instant.fromEpochSeconds(i.toLong()))
            }

            // Specific messages with later timestamps
            max.said("Another weekend, another win 🏆", Instant.fromEpochSeconds(10000))
            lewis.said("The championship is far from over", Instant.fromEpochSeconds(10001))
            charles.said("We're coming for you both! 💪", Instant.fromEpochSeconds(10002))
        }
    }

    community(
        name = "Red Bull Racing",
        avatarUrl = Url("https://example.com/redbull.jpg"),
        inviteCode = "rb_racing",
    ) {
        member(max)
        member(checo)
        member(christian)
    }.apply {
        conversation {
            // Random messages with earlier timestamps
            repeat(300) { i ->
                val randomMembers = listOf(max, checo, christian)
                val randomSender = randomMembers.random()
                randomSender.said("Red Bull internal chat ${i+1}", Instant.fromEpochSeconds(i.toLong()))
            }

            // Specific messages with later timestamps
            max.said("Feeling unstoppable this season", Instant.fromEpochSeconds(20000))
            checo.said("Teamwork makes the dream work", Instant.fromEpochSeconds(20001))
            christian.said("Proud of our performance", Instant.fromEpochSeconds(20002))
        }
    }

    community(
        name = "Mercedes AMG Petronas",
        avatarUrl = Url("https://example.com/mercedes.jpg"),
        inviteCode = "silver_arrows",
    ) {
        member(lewis)
        member(george)
        member(toto)
    }.apply {
        conversation {
            // Random messages with earlier timestamps
            repeat(300) { i ->
                val randomMembers = listOf(lewis, george, toto)
                val randomSender = randomMembers.random()
                randomSender.said("Mercedes team chat ${i+1}", Instant.fromEpochSeconds(i.toLong()))
            }

            // Specific messages with later timestamps
            lewis.said("We're not giving up", Instant.fromEpochSeconds(30000))
            george.said("Learning and improving", Instant.fromEpochSeconds(30001))
            toto.said("Strategy is key", Instant.fromEpochSeconds(30002))
        }
    }

    community(
        name = "Scuderia Ferrari",
        avatarUrl = Url("https://example.com/ferrari.jpg"),
        inviteCode = "cavallino_rampante",
    ) {
        member(charles)
        member(carlos)
        member(mattia)
    }.apply {
        conversation {
            // Random messages with earlier timestamps
            repeat(300) { i ->
                val randomMembers = listOf(charles, carlos, mattia)
                val randomSender = randomMembers.random()
                randomSender.said("Ferrari internal chat ${i+1}", Instant.fromEpochSeconds(i.toLong()))
            }

            // Specific messages with later timestamps
            charles.said("Forza Ferrari! 🇮🇹", Instant.fromEpochSeconds(40000))
            carlos.said("Working together for victory", Instant.fromEpochSeconds(40001))
            mattia.said("Our time is coming", Instant.fromEpochSeconds(40002))
        }
    }

    conversation {
        // Random messages with earlier timestamps
        repeat(200) { i ->
            val randomMembers = listOf(max, checo, lewis, george, charles, carlos, lando, oscar, pierre, esteban)
            val randomSender = randomMembers.random()
            randomSender.said("Cross-team banter ${i+1}", Instant.fromEpochSeconds(i.toLong()))
        }
    }
}