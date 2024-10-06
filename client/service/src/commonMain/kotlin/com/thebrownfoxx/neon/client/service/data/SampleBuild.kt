package com.thebrownfoxx.neon.client.service.data

import com.thebrownfoxx.neon.common.extension.ago
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.type.Url
import kotlin.time.Duration.Companion.minutes

@Suppress("unused")
private fun sampleBuild() = serviceData {
    var lando: MemberId? = null
    var carlos: MemberId? = null
    var charles: MemberId? = null

    community(
        name = "Formula 1",
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
            password = "charles leclerc",
        )
    }.conversation {
        charles?.said("Sup", 15.minutes.ago)
        carlos?.said("Hi", 10.minutes.ago)
        lando?.said("Hello", 5.minutes.ago)
    }

    conversation(*arrayOf(lando, carlos).requireNoNulls()) {
        lando?.said("Sup", 15.minutes.ago)
        carlos?.said("Hi", 10.minutes.ago)
    }
}