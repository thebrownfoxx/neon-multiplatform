package com.thebrownfoxx.neon.client.repository.remote.websocket

import com.thebrownfoxx.neon.client.repository.remote.RemoteMessageDataSource
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.SingleCache
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.websocket.OldWebSocketSession
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.model.TimestampedMessageId
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsUnexpectedError
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageUnexpectedError
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessagesGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessagesRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessagesSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessagesUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessagesUnexpectedError
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.plus

class WebSocketRemoteMessageDataSource(
    private val session: OldWebSocketSession,
) : RemoteMessageDataSource {
    private val dataSourceScope = CoroutineScope(Dispatchers.Default) + SupervisorJob()

    private val conversationPreviewsCache =
        SingleCache<Outcome<List<Message>, DataOperationError>>(dataSourceScope)

    private val messagesCache =
        Cache<GroupId, Outcome<Set<TimestampedMessageId>, DataOperationError>>(dataSourceScope)

    private val messageCache = Cache<MessageId, Outcome<Message, GetError>>(dataSourceScope)

    init {
        session.subscribe<GetConversationPreviewsMemberNotFound> {
            conversationPreviewsCache.emit(Failure(DataOperationError.UnexpectedError))
        }
        session.subscribe<GetConversationPreviewsUnexpectedError> {
            conversationPreviewsCache.emit(Failure(DataOperationError.UnexpectedError))
        }
        session.subscribe<GetConversationPreviewsSuccessful> { response ->
            conversationPreviewsCache.emit(Success(response.conversationPreviews))
        }

        session.subscribe<GetMessagesUnauthorized> { response ->
            messagesCache.emit(response.groupId, Failure(DataOperationError.UnexpectedError))
        }
        session.subscribe<GetMessagesGroupNotFound> { response ->
            messagesCache.emit(response.groupId, Failure(DataOperationError.UnexpectedError))
        }
        session.subscribe<GetMessagesUnexpectedError> { response ->
            messagesCache.emit(response.groupId, Failure(DataOperationError.UnexpectedError))
        }
        session.subscribe<GetMessagesSuccessful> { response ->
            messagesCache.emit(response.groupId, Success(response.messages))
        }

        session.subscribe<GetMessageUnauthorized> { response ->
            messageCache.emit(response.id, Failure(GetError.UnexpectedError))
        }
        session.subscribe<GetMessageNotFound> { response ->
            messageCache.emit(response.id, Failure(GetError.NotFound))
        }
        session.subscribe<GetMessageUnexpectedError> { response ->
            messageCache.emit(response.id, Failure(GetError.UnexpectedError))
        }
        session.subscribe<GetMessageSuccessful> { response ->
            messageCache.emit(response.message.id, Success(response.message))
        }
    }

    override val conversationPreviews: Flow<Outcome<List<Message>, DataOperationError>> =
        conversationPreviewsCache.getAsFlow {
            session.send(GetConversationPreviewsRequest()).onFailure {
                conversationPreviewsCache.emit(Failure(DataOperationError.ConnectionError))
            }
        }

    override fun getMessagesAsFlow(
        groupId: GroupId,
    ): Flow<Outcome<Set<TimestampedMessageId>, DataOperationError>> {
        return messagesCache.getAsFlow(groupId) {
            session.send(GetMessagesRequest(groupId)).onFailure {
                messagesCache.emit(groupId, Failure(DataOperationError.ConnectionError))
            }
        }
    }

    override fun getMessageAsFlow(id: MessageId): Flow<Outcome<Message, GetError>> {
        return messageCache.getAsFlow(id) {
            session.send(GetMessageRequest(id)).onFailure {
                conversationPreviewsCache.emit(Failure(DataOperationError.ConnectionError))
            }
        }
    }
}