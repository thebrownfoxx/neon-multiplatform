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
import com.thebrownfoxx.neon.client.service.default.KtorClientWebSocketConnector
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstGroupManager
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstMemberManager
import com.thebrownfoxx.neon.client.service.offinefirst.OfflineFirstMessenger
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
        val remoteGroupManager = RemoteGroupManager(
            subscriber = webSocketSubscriber,
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
            subscriber = webSocketSubscriber,
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
            subscriber = webSocketSubscriber,
            requester = webSocketRequester,
            externalScope = externalScope,
        )
        val localMessageRepository = ExposedMessageRepository(
            database = database,
            getMemberId = { authenticator.loggedInMemberId.filterNotNull().first() },
            externalScope = externalScope,
        )
        OfflineFirstMessenger(
            authenticator = authenticator,
            remoteMessenger = remoteMessenger,
            localMessageRepository = localMessageRepository,
            externalScope = externalScope,
        )
    }
}