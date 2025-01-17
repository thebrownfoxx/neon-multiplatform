package com.thebrownfoxx.neon.client.application.dummy

import com.thebrownfoxx.neon.client.service.Dependencies
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class DummyDependencies : Dependencies {
    override val authenticator = DummyAuthenticator()
    override val groupManager = DummyGroupManager(getGroupDelay = 100.milliseconds)
    override val memberManager = DummyMemberManager()
    override val messenger = DummyMessenger(
        conversationPreviewsDelay = 5.seconds,
        getMessageDelay = 2.seconds,
    )
}