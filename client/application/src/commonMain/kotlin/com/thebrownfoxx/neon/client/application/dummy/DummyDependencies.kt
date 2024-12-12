package com.thebrownfoxx.neon.client.application.dummy

import com.thebrownfoxx.neon.client.service.Dependencies
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.default.InMemoryTokenStorage
import com.thebrownfoxx.neon.common.PrintLogger
import com.thebrownfoxx.outcome.Outcome
import kotlin.time.Duration.Companion.seconds

class DummyDependencies : Dependencies {
    override val logger = PrintLogger
    override val tokenStorage = InMemoryTokenStorage()
    override val authenticator = DummyAuthenticator()
    override val groupManager = DummyGroupManager(getGroupDelay = 2.seconds)
    override val memberManager = DummyMemberManager()
    override val messenger = DummyMessenger()

    @Deprecated("Use groupManager instead")
    override suspend fun getGroupManager(): Outcome<GroupManager, Dependencies.GetGroupManagerError> {
        TODO("Not yet implemented")
    }
}