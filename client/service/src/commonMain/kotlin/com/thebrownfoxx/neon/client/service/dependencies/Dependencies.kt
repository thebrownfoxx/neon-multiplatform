package com.thebrownfoxx.neon.client.service.dependencies

import com.thebrownfoxx.neon.client.service.authenticator.Authenticator
import com.thebrownfoxx.neon.client.service.dependencies.model.GetGroupManagerError
import com.thebrownfoxx.neon.client.service.group.GroupManager
import com.thebrownfoxx.neon.client.service.jwt.TokenStorage
import com.thebrownfoxx.neon.common.outcome.Outcome

interface Dependencies {
    val tokenStorage: TokenStorage
    val authenticator: Authenticator
    suspend fun getGroupManager(): Outcome<GroupManager, GetGroupManagerError>
}