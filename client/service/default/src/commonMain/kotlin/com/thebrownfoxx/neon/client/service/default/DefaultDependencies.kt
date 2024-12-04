package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.repository.local.exposed.ExposedLocalGroupDataSource
import com.thebrownfoxx.neon.client.repository.offlinefirst.OfflineFirstGroupRepository
import com.thebrownfoxx.neon.client.repository.remote.websocket.WebSocketRemoteGroupDataSource
import com.thebrownfoxx.neon.client.service.dependencies.Dependencies
import com.thebrownfoxx.neon.client.service.dependencies.model.GetGroupRepositoryError
import com.thebrownfoxx.neon.client.service.group.GroupManager
import com.thebrownfoxx.neon.client.websocket.model.ConnectWebSocketError
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.asFailure
import com.thebrownfoxx.neon.common.outcome.getOrElse
import io.ktor.client.HttpClient
import org.jetbrains.exposed.sql.Database

class DefaultDependencies(
    httpClient: HttpClient,
    private val database: Database,
) : Dependencies {
    override val tokenStorage = InMemoryTokenStorage()

    override val authenticator = RemoteAuthenticator(httpClient, tokenStorage)

    private val webSocketProvider = KtorClientWebSocketProvider(
        httpClient,
        tokenStorage,
        authenticator,
    )

    override suspend fun getGroupManager(): Outcome<GroupManager, GetGroupRepositoryError> {
        val localDataSource = ExposedLocalGroupDataSource(database)

        val webSocketSession = webSocketProvider.getSession().getOrElse { error ->
            return when (error) {
                ConnectWebSocketError.Unauthorized -> GetGroupRepositoryError.Unauthorized
                ConnectWebSocketError.ConnectionError -> GetGroupRepositoryError.ConnectionError
            }.asFailure()
        }

        val remoteDataSource = WebSocketRemoteGroupDataSource(webSocketSession)

        val repository = OfflineFirstGroupRepository(localDataSource, remoteDataSource)

        return Success(DefaultGroupManager(repository))
    }
}