package com.thebrownfoxx.neon.client.service.data

import com.thebrownfoxx.neon.common.extension.ago
import kotlin.time.Duration.Companion.minutes

private fun main() {
    val serviceData = serviceData {
        val landoNorris = member(
            username = "lando_norris",
            avatar = null,
            password = "carlos sainz",
        )
        val carlosSainz = member(
            username = "carlos_sainz",
            avatar = null,
            password = "lando norris",
        )
        val charlesLeclerc = member(
            username = "charles_leclerc",
            avatar = null,
            password = "carlos sainz",
        )

        val formula1 = community(
            name = "Formula 1",
            avatarUrl = null,
            members = listOf(landoNorris, carlosSainz, charlesLeclerc),
        )

        formula1.conversation {
            landoNorris.said(
                "WTF?! charles kissed carlos?!",
                10.minutes.ago,
            )
            charlesLeclerc.said(
                "I'm sorry, Lando. It was a mistake.",
                9.minutes.ago,
            )
        }

        conversation(carlosSainz, landoNorris) {
            carlosSainz.said(
                "I'm sorry, Lando. I swear I didn't kiss back.",
                10.minutes.ago,
            )
        }
    }

    println(serviceData)
}