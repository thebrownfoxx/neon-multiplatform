package com.thebrownfoxx.neon.client.service.dependencies

import com.thebrownfoxx.neon.client.service.authenticator.Authenticator
import com.thebrownfoxx.neon.client.service.dependencies.model.GetGroupRepositoryError
import com.thebrownfoxx.neon.client.service.group.GroupManager
import com.thebrownfoxx.neon.client.service.jwt.TokenStorage
import com.thebrownfoxx.neon.common.type.Outcome

interface Dependencies {
    val tokenStorage: TokenStorage
    val authenticator: Authenticator
    suspend fun getGroupManager(): Outcome<GroupManager, GetGroupRepositoryError>
}