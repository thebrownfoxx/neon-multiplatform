package com.thebrownfoxx.neon.client.application

import androidx.lifecycle.viewmodel.CreationExtras
import com.thebrownfoxx.neon.client.remote.service.KtorClientWebSocketProvider
import com.thebrownfoxx.neon.client.remote.service.RemoteAuthenticator
import com.thebrownfoxx.neon.client.remote.service.RemoteGroupManager
import com.thebrownfoxx.neon.client.remote.service.RemoteMemberManager
import com.thebrownfoxx.neon.client.remote.service.RemoteMessenger
import com.thebrownfoxx.neon.client.service.Dependencies
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.service.MemberManager
import com.thebrownfoxx.neon.client.service.Messenger
import com.thebrownfoxx.neon.client.service.default.InMemoryTokenStorage
import com.thebrownfoxx.neon.client.websocket.AlwaysActiveWebSocketSession
import com.thebrownfoxx.neon.common.PrintLogger
import com.thebrownfoxx.outcome.map.onSuccess
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database

expect val CreationExtras.dependencies: Dependencies

class AppDependencies(
    httpClient: HttpClient,
    private val database: Database,
    externalScope: CoroutineScope,
) : Dependencies {
    override val logger = PrintLogger
    override val tokenStorage = InMemoryTokenStorage()
    override val authenticator = RemoteAuthenticator(httpClient, tokenStorage)

    private val webSocketProvider = KtorClientWebSocketProvider(
        httpClient = httpClient,
        tokenStorage = tokenStorage,
        authenticator = authenticator,
        externalScope = externalScope,
        logger = logger,
    )
    private val webSocketSession = AlwaysActiveWebSocketSession(logger, externalScope)

    override val groupManager: GroupManager = RemoteGroupManager(
        subscriber = webSocketSession,
        externalScope = externalScope,
    )

    override val memberManager: MemberManager = RemoteMemberManager(
        subscriber = webSocketSession,
        externalScope = externalScope,
    )

    override val messenger: Messenger = RemoteMessenger(
        authenticator = authenticator,
        subscriber = webSocketSession,
        requester = webSocketSession,
        externalScope = externalScope,
        logger = logger,
    )

    init {
        externalScope.launch {
            tokenStorage.token.collect {
                it.onSuccess {
                    webSocketSession.connect { webSocketProvider.getSession() }
                }
            }
        }
    }
}