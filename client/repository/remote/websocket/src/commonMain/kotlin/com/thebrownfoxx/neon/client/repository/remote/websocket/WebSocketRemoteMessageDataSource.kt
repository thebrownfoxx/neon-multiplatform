package com.thebrownfoxx.neon.client.repository.remote.websocket

import com.thebrownfoxx.neon.client.repository.remote.GetConversationPreviewsError
import com.thebrownfoxx.neon.client.repository.remote.GetMessageError
import com.thebrownfoxx.neon.client.repository.remote.RemoteMessageDataSource
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.SingleCache
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsInternalError
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageInternalError
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageUnauthorized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.plus

class WebSocketRemoteMessageDataSource(
    private val session: WebSocketSession,
) : RemoteMessageDataSource {
    private val dataSourceScope = CoroutineScope(Dispatchers.Default) + SupervisorJob()

    private val conversationsCache =
        SingleCache<Outcome<List<Message>, GetConversationPreviewsError>>(dataSourceScope)

    private val messageCache = Cache<MessageId, Outcome<Message, GetMessageError>>(dataSourceScope)

    init {
        session.subscribe<GetConversationPreviewsMemberNotFound> {
            conversationsCache.emit(Failure(GetConversationPreviewsError.Invalid))
        }
        session.subscribe<GetConversationPreviewsInternalError> {
            conversationsCache.emit(Failure(GetConversationPreviewsError.ServerError))
        }
        session.subscribe<GetConversationPreviewsSuccessful> { response ->
            conversationsCache.emit(Success(response.conversations))
        }

        session.subscribe<GetMessageUnauthorized> { response ->
            messageCache.emit(response.id, Failure(GetMessageError.Invalid))
        }
        session.subscribe<GetMessageInternalError> { response ->
            messageCache.emit(response.id, Failure(GetMessageError.ServerError))
        }
        session.subscribe<GetMessageSuccessful> { response ->
            messageCache.emit(response.message.id, Success(response.message))
        }
    }

    override val conversationPreviews: Flow<Outcome<List<Message>, GetConversationPreviewsError>> =
        conversationsCache.getAsFlow {
            session.send(GetConversationsRequest())
        }

    override fun getMessageAsFlow(id: MessageId): Flow<Outcome<Message, GetMessageError>> =
        messageCache.getAsFlow(id) {
            session.send(GetMessageRequest(id))
        }
}