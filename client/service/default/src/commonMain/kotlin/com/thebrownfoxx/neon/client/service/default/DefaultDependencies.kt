package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.repository.local.exposed.ExposedLocalGroupDataSource
import com.thebrownfoxx.neon.client.repository.offlinefirst.OfflineFirstGroupRepository
import com.thebrownfoxx.neon.client.repository.remote.websocket.WebSocketRemoteGroupDataSource
import com.thebrownfoxx.neon.client.service.Dependencies
import com.thebrownfoxx.neon.client.service.Dependencies.GetGroupManagerError
import com.thebrownfoxx.neon.client.service.GroupManager
import com.thebrownfoxx.neon.client.websocket.model.ConnectWebSocketError
import com.thebrownfoxx.neon.common.PrintLogger
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.getOrElse
import com.thebrownfoxx.outcome.mapError
import io.ktor.client.HttpClient
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