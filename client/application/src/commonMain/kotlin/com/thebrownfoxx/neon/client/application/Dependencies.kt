package com.thebrownfoxx.neon.client.application

import androidx.lifecycle.viewmodel.CreationExtras
import com.thebrownfoxx.neon.client.remote.websocket.WebSocketRemoteGroupManager
import com.thebrownfoxx.neon.client.remote.websocket.WebSocketRemoteMemberManager
import com.thebrownfoxx.neon.client.remote.websocket.WebSocketRemoteMessenger
import com.thebrownfoxx.neon.client.repository.exposed.ExposedLocalDeliveryRepository
import com.thebrownfoxx.neon.client.repository.exposed.ExposedLocalGroupMemberRepository
import com.thebrownfoxx.neon.client.repository.exposed.ExposedLocalGroupRepository
import com.thebrownfoxx.neon.client.repository.exposed.ExposedLocalMemberRepository
import com.thebrownfoxx.neon.client.repository.exposed.ExposedLocalMessageRepository
import com.thebrownfoxx.neon.client.repository.exposed.ExposedTokenRepository
import com.thebrownfoxx.neon.client.service.Dependencies
import com.thebrownfoxx.neon.client.service.default.DefaultAuthenticator
import com.thebrownfoxx.neon.client.service.default.KtorClientWebSocketConnector
import com.thebrownfoxx.neon.client.service.offinefirst.group.OfflineFirstGroupManager
import com.thebrownfoxx.neon.client.service.offinefirst.member.OfflineFirstMemberManager
import com.thebrownfoxx.neon.client.service.offinefirst.mesage.OfflineFirstMessenger
import com.thebrownfoxx.neon.client.websocket.AutoConnectWebSocketSessionProvider
import com.thebrownfoxx.neon.client.websocket.AutoRetryWebSocketRequester
import com.thebrownfoxx.neon.client.websocket.AutoRetryWebSocketSubscriber
import com.thebrownfoxx.outcome.Success
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import org.jetbrains.exposed.sql.Database

expect val CreationExtras.dependencies: Dependencies

class AppDependencies(
    httpClient: HttpClient,
    private val database: Database,
    externalScope: CoroutineScope,
) : Dependencies {

    private val tokenRepository = ExposedTokenRepository(database, externalScope)

    override val authenticator = DefaultAuthenticator(
        httpClient,
        tokenRepository,
        externalScope,
    )

    private val webSocketConnector = KtorClientWebSocketConnector(
        httpClient = httpClient,
        externalScope = externalScope,
    )

    private val token = tokenRepository.getAsFlow().transform { token ->
        if (token is Success) emit(token.value.jwt)
    }

    private val webSocketSessionProvider = AutoConnectWebSocketSessionProvider(
        token = token,
        connector = webSocketConnector,
        externalScope = externalScope,
    )

    private val webSocketSubscriber = AutoRetryWebSocketSubscriber(webSocketSessionProvider)
    private val webSocketRequester = AutoRetryWebSocketRequester(webSocketSessionProvider)

    override val groupManager = run {
        val remoteGroupManager = WebSocketRemoteGroupManager(
            subscriber = webSocketSubscriber,
            externalScope = externalScope,
        )
        val localGroupRepository = ExposedLocalGroupRepository(database, externalScope)
        val localGroupMemberRepository = ExposedLocalGroupMemberRepository(database, externalScope)
        OfflineFirstGroupManager(
            remoteGroupManager = remoteGroupManager,
            localGroupRepository = localGroupRepository,
            localGroupMemberRepository = localGroupMemberRepository,
            externalScope = externalScope,
        )
    }

    override val memberManager = run {
        val remoteMemberManager = WebSocketRemoteMemberManager(
            subscriber = webSocketSubscriber,
            externalScope = externalScope,
        )
        val localMemberRepository = ExposedLocalMemberRepository(database, externalScope)
        OfflineFirstMemberManager(
            remoteMemberManager = remoteMemberManager,
            localMemberRepository = localMemberRepository,
            externalScope = externalScope,
        )
    }

    override val messenger = run {
        val remoteMessenger = WebSocketRemoteMessenger(
            subscriber = webSocketSubscriber,
            requester = webSocketRequester,
            externalScope = externalScope,
        )
        val localMessageRepository = ExposedLocalMessageRepository(
            database = database,
            getMemberId = { authenticator.loggedInMemberId.filterNotNull().first() },
            externalScope = externalScope,
        )
        val localDeliveryRepository = ExposedLocalDeliveryRepository(
            database = database,
            externalScope = externalScope,
        )
        OfflineFirstMessenger(
            authenticator = authenticator,
            remoteMessenger = remoteMessenger,
            localMessageRepository = localMessageRepository,
            localDeliveryRepository = localDeliveryRepository,
            externalScope = externalScope,
        )
    }
}