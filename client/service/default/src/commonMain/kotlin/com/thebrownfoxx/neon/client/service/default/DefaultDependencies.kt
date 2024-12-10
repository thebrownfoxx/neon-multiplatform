package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.repository.local.exposed.ExposedLocalGroupDataSource
import com.thebrownfoxx.neon.client.repository.local.exposed.ExposedLocalMemberDataSource
import com.thebrownfoxx.neon.client.repository.local.exposed.ExposedLocalMessageDataSource
import com.thebrownfoxx.neon.client.repository.offlinefirst.OfflineFirstGroupRepository
import com.thebrownfoxx.neon.client.repository.offlinefirst.OfflineFirstMemberRepository
import com.thebrownfoxx.neon.client.repository.offlinefirst.OfflineFirstMessageRepository
import com.thebrownfoxx.neon.client.repository.remote.websocket.WebSocketRemoteGroupDataSource
import com.thebrownfoxx.neon.client.repository.remote.websocket.WebSocketRemoteMemberDataSource
import com.thebrownfoxx.neon.client.repository.remote.websocket.WebSocketRemoteMessageDataSource
import com.thebrownfoxx.neon.client.service.Dependencies
import com.thebrownfoxx.neon.client.service.Dependencies.GetGroupManagerError
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.websocket.AlwaysActiveWebSocketSession
import com.thebrownfoxx.neon.client.websocket.ConnectWebSocketError
import com.thebrownfoxx.neon.common.PrintLogger
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.getOrElse
import com.thebrownfoxx.outcome.mapError
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.first
import org.jetbrains.exposed.sql.Database

class DefaultDependencies(
    httpClient: HttpClient,
    private val database: Database,
) : Dependencies {
    override val tokenStorage = InMemoryTokenStorage()

    override val authenticator = RemoteAuthenticator(httpClient, tokenStorage)

    private val logger = PrintLogger

    private val webSocketProvider = KtorClientWebSocketProvider(
        httpClient,
        tokenStorage,
        authenticator,
        logger,
    )

    private val webSocketSession = AlwaysActiveWebSocketSession(logger).apply {
        connect { webSocketProvider.getSession() }
    }

    override val groupManager = run {
        val localDataSource = ExposedLocalGroupDataSource(database)
        val remoteDataSource = WebSocketRemoteGroupDataSource(webSocketSession)
        val repository = OfflineFirstGroupRepository(localDataSource, remoteDataSource)
        DefaultGroupManager(repository)
    }

    override val memberManager = run {
        val localDataSource = ExposedLocalMemberDataSource(database)
        val remoteDataSource = WebSocketRemoteMemberDataSource(webSocketSession)
        val repository = OfflineFirstMemberRepository(localDataSource, remoteDataSource)
        DefaultMemberManager(repository)
    }

    override val messenger = run {
        val localDataSource = ExposedLocalMessageDataSource(
            database,
            getMemberId = { authenticator.loggedInMember.first { it != null }!! }, // TODO: T-T
        )
        val remoteDataSource = WebSocketRemoteMessageDataSource(webSocketSession, logger)
        val repository = OfflineFirstMessageRepository(localDataSource, remoteDataSource)
        DefaultMessenger(repository)
    }

    @Deprecated("Use groupManager instead")
    override suspend fun getGroupManager(): Outcome<GroupManager, GetGroupManagerError> {
        val localDataSource = ExposedLocalGroupDataSource(database)

        val webSocketSession = webSocketProvider.getSession().getOrElse {
            return mapError(error.toGetGroupManagerError())
        }

        val remoteDataSource = WebSocketRemoteGroupDataSource(webSocketSession)

        val repository = OfflineFirstGroupRepository(localDataSource, remoteDataSource)

        return Success(DefaultGroupManager(repository))
    }

    private fun ConnectWebSocketError.toGetGroupManagerError() = when (this) {
        ConnectWebSocketError.Unauthorized -> GetGroupManagerError.Unauthorized
        ConnectWebSocketError.ConnectionError -> GetGroupManagerError.ConnectionError
    }
}