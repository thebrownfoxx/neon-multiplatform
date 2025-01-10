package com.thebrownfoxx.neon.client.application.dummy

import com.thebrownfoxx.neon.client.service.Dependencies
import com.thebrownfoxx.neon.common.PrintLogger

class DummyDependencies : Dependencies {
    override val logger = PrintLogger
    override val authenticator = DummyAuthenticator()
    override val groupManager = DummyGroupManager()
    override val memberManager = DummyMemberManager()
    override val messenger = DummyMessenger()
}