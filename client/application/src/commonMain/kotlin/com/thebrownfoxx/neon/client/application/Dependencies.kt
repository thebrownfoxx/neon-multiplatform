package com.thebrownfoxx.neon.client.application

import androidx.lifecycle.viewmodel.CreationExtras
import com.thebrownfoxx.neon.client.remote.service.RemoteGroupManager
import com.thebrownfoxx.neon.client.remote.service.RemoteMemberManager
import com.thebrownfoxx.neon.client.remote.service.RemoteMessenger
import com.thebrownfoxx.neon.client.repository.exposed.ExposedGroupMemberRepository
import com.thebrownfoxx.neon.client.repository.exposed.ExposedGroupRepository
import com.thebrownfoxx.neon.client.repository.exposed.ExposedMemberRepository
import com.thebrownfoxx.neon.client.repository.exposed.ExposedMessageRepository
import com.thebrownfoxx.neon.client.repository.exposed.ExposedTokenRepository
import com.thebrownfoxx.neon.client.service.Dependencies
import com.thebrownfoxx.neon.client.service.default.DefaultAuthenticator
import com.thebrownfoxx.neon.client.service.default.KtorClientWebSocketProvider
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstGroupManager
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstMemberManager
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstMessenger
import com.thebrownfoxx.neon.client.websocket.AlwaysActiveWebSocketSession
import com.thebrownfoxx.neon.common.PrintLogger
import com.thebrownfoxx.outcome.map.onSuccess
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database

expect val CreationExtras.dependencies: Dependencies

class AppDependencies(
    httpClient: HttpClient,
    private val database: Database,
    externalScope: CoroutineScope,
) : Dependencies {
    override val logger = PrintLogger

    private val tokenRepository = ExposedTokenRepository(database, externalScope)

    override val authenticator = DefaultAuthenticator(
        httpClient,
        tokenRepository,
        externalScope,
    )

    private val webSocketProvider = KtorClientWebSocketProvider(
        httpClient = httpClient,
        tokenRepository = tokenRepository,
        authenticator = authenticator,
        externalScope = externalScope,
        logger = logger,
    )
    private val webSocketSession = AlwaysActiveWebSocketSession(logger)

    override val groupManager = run {
        val remoteGroupManager = RemoteGroupManager(
            subscriber = webSocketSession,
            externalScope = externalScope,
        )
        val localGroupRepository = ExposedGroupRepository(database, externalScope)
        val localGroupMemberRepository = ExposedGroupMemberRepository(database, externalScope)
        OfflineFirstGroupManager(
            remoteGroupManager = remoteGroupManager,
            localGroupRepository = localGroupRepository,
            localGroupMemberRepository = localGroupMemberRepository,
            externalScope = externalScope,
        )
    }

    override val memberManager = run {
        val remoteMemberManager = RemoteMemberManager(
            subscriber = webSocketSession,
            externalScope = externalScope,
        )
        val localMemberRepository = ExposedMemberRepository(database, externalScope)
        OfflineFirstMemberManager(
            remoteMemberManager = remoteMemberManager,
            localMemberRepository = localMemberRepository,
            externalScope = externalScope,
        )
    }

    override val messenger = run {
        val remoteMessenger = RemoteMessenger(
            authenticator = authenticator,
            subscriber = webSocketSession,
            requester = webSocketSession,
            externalScope = externalScope,
        )
        val localMessageRepository = ExposedMessageRepository(
            database = database,
            getMemberId = { authenticator.loggedInMemberId.filterNotNull().first() },
            externalScope = externalScope,
        )
        OfflineFirstMessenger(
            remoteMessenger = remoteMessenger,
            localMessageRepository = localMessageRepository,
            externalScope = externalScope,
        )
    }

    init {
        externalScope.launch {
            tokenRepository.getAsFlow().collect {
                it.onSuccess {
                    webSocketSession.connect { webSocketProvider.getSession() }
                }
            }
        }
    }
}