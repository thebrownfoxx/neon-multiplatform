package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.repository.local.exposed.ExposedLocalGroupDataSource
import com.thebrownfoxx.neon.client.repository.local.exposed.ExposedLocalGroupMemberDataSource
import com.thebrownfoxx.neon.client.repository.local.exposed.ExposedLocalMemberDataSource
import com.thebrownfoxx.neon.client.repository.local.exposed.ExposedLocalMessageDataSource
import com.thebrownfoxx.neon.client.repository.offlinefirst.OfflineFirstGroupMemberRepository
import com.thebrownfoxx.neon.client.repository.offlinefirst.OfflineFirstGroupRepository
import com.thebrownfoxx.neon.client.repository.offlinefirst.OfflineFirstMemberRepository
import com.thebrownfoxx.neon.client.repository.offlinefirst.OfflineFirstMessageRepository
import com.thebrownfoxx.neon.client.repository.remote.websocket.WebSocketRemoteGroupDataSource
import com.thebrownfoxx.neon.client.repository.remote.websocket.WebSocketRemoteGroupMemberDataSource
import com.thebrownfoxx.neon.client.repository.remote.websocket.WebSocketRemoteMemberDataSource
import com.thebrownfoxx.neon.client.repository.remote.websocket.WebSocketRemoteMessageDataSource
import com.thebrownfoxx.neon.client.service.Dependencies
import com.thebrownfoxx.neon.client.service.Dependencies.GetGroupManagerError
import com.thebrownfoxx.neon.client.websocket.AlwaysActiveWebSocketSession
import com.thebrownfoxx.neon.client.websocket.WebSocketConnectionError
import com.thebrownfoxx.neon.common.PrintLogger
import com.thebrownfoxx.neon.common.websocket.asOldWebSocketSession
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import kotlin.time.Duration.Companion.seconds

class DefaultDependencies(
    httpClient: HttpClient,
    private val database: Database,
) : Dependencies {
    override val logger = PrintLogger

    override val tokenStorage = InMemoryTokenStorage()

    override val authenticator = RemoteAuthenticator(httpClient, tokenStorage)

    private val webSocketProvider = KtorClientWebSocketProvider(
        httpClient,
        tokenStorage,
        authenticator,
    )

    private val webSocketSession = AlwaysActiveWebSocketSession(logger)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            var done = false
            while (!done) {
                tokenStorage.get()
                    .onSuccess {
                        done = true
                        webSocketSession.connect { webSocketProvider.getSession() }
                    }
                    .onFailure { delay(1.seconds) }
            }
        }
    }

    override val groupManager = run {
        val localGroupDataSource = ExposedLocalGroupDataSource(database)
        val remoteGroupDataSource =
            WebSocketRemoteGroupDataSource(webSocketSession.asOldWebSocketSession(logger))
        val groupRepository =
            OfflineFirstGroupRepository(localGroupDataSource, remoteGroupDataSource)
        val groupMemberLocalDataSource = ExposedLocalGroupMemberDataSource(database)
        val groupMemberRemoteDataSource =
            WebSocketRemoteGroupMemberDataSource(webSocketSession.asOldWebSocketSession(logger))
        val groupMemberRepository = OfflineFirstGroupMemberRepository(
            groupMemberLocalDataSource,
            groupMemberRemoteDataSource,
        )
        DefaultGroupManager(groupRepository, groupMemberRepository)
    }

    override val memberManager = run {
        val localDataSource = ExposedLocalMemberDataSource(database)
        val remoteDataSource =
            WebSocketRemoteMemberDataSource(webSocketSession.asOldWebSocketSession(logger))
        val repository = OfflineFirstMemberRepository(localDataSource, remoteDataSource)
        DefaultMemberManager(repository)
    }

    override val messenger = run {
        val localDataSource = ExposedLocalMessageDataSource(
            database,
            getMemberId = { authenticator.loggedInMember.first { it != null }!! }, // TODO: T-T
        )
        val remoteDataSource =
            WebSocketRemoteMessageDataSource(webSocketSession.asOldWebSocketSession(logger))
        val repository = OfflineFirstMessageRepository(localDataSource, remoteDataSource)
        DefaultMessenger(authenticator, repository, webSocketSession.asOldWebSocketSession(logger))
    }

    private fun WebSocketConnectionError.toGetGroupManagerError() = when (this) {
        WebSocketConnectionError.Unauthorized -> GetGroupManagerError.Unauthorized
        WebSocketConnectionError.ConnectionError -> GetGroupManagerError.ConnectionError
    }
}