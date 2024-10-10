package com.thebrownfoxx.neon.client.service.data

import com.thebrownfoxx.neon.common.extension.ago
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.type.Url
import kotlin.time.Duration.Companion.minutes

class IntegrateServiceDataTest {
    private val serviceData = serviceData {
        lateinit var lando: MemberId
        lateinit var carlos: MemberId
        lateinit var charles: MemberId
        lateinit var oscar: MemberId
        lateinit var andrea: MemberId

        community(
            name = "Formula 1 Drivers",
            avatarUrl = Url("https://example.com/formula1.jpg"),
            inviteCode = "f1",
        ) {
            lando = member(
                username = "lando_norris",
                avatarUrl = null,
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
        }.conversation {
            lando.said("wtf charles?", 6.minutes.ago)
            charles.said("I'm sorry, Lando", 5.minutes.ago)
            carlos.said("im sorry lando. you know im all yours.", 4.minutes.ago)
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
        }.conversation {
            oscar.said("I'm sorry, Lando", 10.minutes.ago)
            lando.said(
                "not cool. first, charles kissed carlos, then you fucked up my race? im having the worst day ever.",
                5.minutes.ago,
            )
            andrea.said("ill suck yalls toes if you stop fighting", 4.minutes.ago)
        }

        conversation {
            carlos.said("hey. im in your bed rn 😉", 2.minutes.ago)
            carlos.said("i lost my clothes 😉", 2.minutes.ago)
            carlos.said("can you cover me up with your body?", 2.minutes.ago)
            lando.said("be right there 🤤", 1.minutes.ago)
        }
    }

    // TODO: Finish writing the tests
}