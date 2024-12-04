package com.thebrownfoxx.neon.client.repository.offlinefirst

import com.thebrownfoxx.neon.client.converter.toLocalMessage
import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.repository.local.LocalMessageDataSource
import com.thebrownfoxx.neon.client.repository.remote.GetMessageError
import com.thebrownfoxx.neon.client.repository.remote.RemoteMessageDataSource
import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.type.id.MessageId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class OfflineFirstMessageRepository(
    private val localDataSource: LocalMessageDataSource,
    private val remoteDataSource: RemoteMessageDataSource,
) : MessageRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.Default) + SupervisorJob()

    override val conversationPreviews = kotlin.run {
        val sharedFlow =
            MutableSharedFlow<Outcome<LocalConversationPreviews, ConnectionError>>(replay = 1)

        coroutineScope.launch {
            localDataSource.conversationPreviews.collect { localConversationPreviewsOutcome ->
                val localConversationPreviews =
                    localConversationPreviewsOutcome.getOrElse { error ->
                        sharedFlow.emit(Failure(ConnectionError))
                        return@collect
                    }
                sharedFlow.emit(Success(localConversationPreviews))
            }
        }

        coroutineScope.launch {
            remoteDataSource.conversationPreviews.collect { remoteConversationPreviewsOutcome ->
                val remoteConversationPreviews =
                    remoteConversationPreviewsOutcome.getOrElse { error ->
                        sharedFlow.emit(Failure(ConnectionError))
                        return@collect
                    }
                localDataSource.batchUpsert(remoteConversationPreviews.map { it.toLocalMessage() })
            }
        }

        sharedFlow.asSharedFlow()
    }

    override fun get(id: MessageId): Flow<Outcome<LocalMessage, GetError>> {
        val sharedFlow = MutableSharedFlow<Outcome<LocalMessage, GetError>>(replay = 1)

        coroutineScope.launch {
            localDataSource.getMessageAsFlow(id).collect { localMessageOutcome ->
                val localMessage = localMessageOutcome.getOrElse { error ->
                    when (error) {
                        GetError.NotFound -> {}
                        GetError.ConnectionError ->
                            sharedFlow.emit(Failure(GetError.ConnectionError))
                    }
                    return@collect
                }
                sharedFlow.emit(Success(localMessage))
            }
        }

        coroutineScope.launch {
            remoteDataSource.getMessageAsFlow(id).collect { remoteMessageOutcome ->
                val remoteMessage = remoteMessageOutcome.getOrElse { error ->
                    val mappedError = when (error) {
                        GetMessageError.NotFound -> GetError.NotFound
                        GetMessageError.ServerError -> GetError.ConnectionError // TODO: Fix errors T-T
                    }
                    sharedFlow.emit(Failure(mappedError))
                    return@collect
                }
                localDataSource.upsert(remoteMessage.toLocalMessage())
            }
        }

        return sharedFlow.asSharedFlow()
    }
}