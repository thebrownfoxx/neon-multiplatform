package com.thebrownfoxx.neon.client.repository.offlinefirst

import com.thebrownfoxx.neon.client.converter.toLocalMessage
import com.thebrownfoxx.neon.client.model.LocalConversationPreviews
import com.thebrownfoxx.neon.client.model.LocalMessage
import com.thebrownfoxx.neon.client.repository.MessageRepository
import com.thebrownfoxx.neon.client.repository.local.LocalMessageDataSource
import com.thebrownfoxx.neon.client.repository.local.LocalMessageDataSource.LocalTimestampedMessageId
import com.thebrownfoxx.neon.client.repository.remote.RemoteMessageDataSource
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.UnitOutcome
import com.thebrownfoxx.outcome.map.getOrElse
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
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    override val conversationPreviewsFlow:
            Flow<Outcome<LocalConversationPreviews, DataOperationError>> = run {
        val sharedFlow =
            MutableSharedFlow<Outcome<LocalConversationPreviews, DataOperationError>>(replay = 1)

        coroutineScope.launch {
            localDataSource.conversationPreviews.collect { localConversationPreviewsOutcome ->
                val localConversationPreviews =
                    localConversationPreviewsOutcome.getOrElse { error ->
                        sharedFlow.emit(Failure(error))
                        return@collect
                    }
                sharedFlow.emit(Success(localConversationPreviews))
            }
        }

        coroutineScope.launch {
            remoteDataSource.conversationPreviews.collect { remoteConversationPreviewsOutcome ->
                val remoteConversationPreviews =
                    remoteConversationPreviewsOutcome.getOrElse { error ->
                        sharedFlow.emit(Failure(error))
                        return@collect
                    }
                localDataSource.batchUpsert(remoteConversationPreviews.map { it.toLocalMessage() })
            }
        }

        sharedFlow.asSharedFlow()
    }

    override fun getMessagesAsFlow(
        groupId: GroupId,
    ): Flow<Outcome<Set<MessageId>, DataOperationError>> {
        val sharedFlow = MutableSharedFlow<Outcome<Set<MessageId>, DataOperationError>>(replay = 1)

        coroutineScope.launch {
            localDataSource.getMessagesAsFlow(groupId).collect { localMessagesOutcome ->
                val localMessages = localMessagesOutcome.getOrElse { error ->
                    sharedFlow.emit(Failure(error))
                    return@collect
                }
                sharedFlow.emit(Success(localMessages))
            }
        }

        coroutineScope.launch {
            remoteDataSource.getMessagesAsFlow(groupId).collect { remoteMessagesOutcome ->
                val remoteMessages = remoteMessagesOutcome.getOrElse { error ->
                    sharedFlow.emit(Failure(error))
                    return@collect
                }.map {
                    LocalTimestampedMessageId(
                        id = it.id,
                        groupId = groupId,
                        timestamp = it.timestamp,
                    )
                }.toSet()
                localDataSource.batchUpsert(remoteMessages)
            }
        }

        return sharedFlow.asSharedFlow()
    }

    override fun getAsFlow(id: MessageId): Flow<Outcome<LocalMessage, GetError>> {
        val sharedFlow = MutableSharedFlow<Outcome<LocalMessage, GetError>>(replay = 1)

        coroutineScope.launch {
            localDataSource.getMessageAsFlow(id).collect { localMessageOutcome ->
                val localMessage = localMessageOutcome.getOrElse { error ->
                    when (error) {
                        GetError.NotFound -> {}
                        GetError.ConnectionError, GetError.UnexpectedError ->
                            sharedFlow.emit(Failure(error))
                    }
                    return@collect
                }
                sharedFlow.emit(Success(localMessage))
            }
        }

        coroutineScope.launch {
            remoteDataSource.getMessageAsFlow(id).collect { remoteMessageOutcome ->
                val remoteMessage = remoteMessageOutcome.getOrElse { error ->
//                    sharedFlow.emit(Failure(error))
                    return@collect
                }
                localDataSource.upsert(remoteMessage.toLocalMessage())
            }
        }

        return sharedFlow.asSharedFlow()
    }

    override fun getOutgoingMessagesAsFlow():
            Flow<Outcome<List<LocalMessage>, DataOperationError>> {
        return localDataSource.getOutgoingMessagesAsFlow()
    }

    override suspend fun upsert(
        message: LocalMessage,
    ): UnitOutcome<DataOperationError> {
        return localDataSource.upsert(message)
    }
}