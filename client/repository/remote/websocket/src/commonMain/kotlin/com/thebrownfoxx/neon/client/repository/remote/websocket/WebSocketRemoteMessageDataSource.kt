package com.thebrownfoxx.neon.client.repository.remote.websocket

import com.thebrownfoxx.neon.client.repository.remote.RemoteMessageDataSource
import com.thebrownfoxx.neon.common.Logger
import com.thebrownfoxx.neon.common.data.Cache
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.data.SingleCache
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.websocket.WebSocketSession
import com.thebrownfoxx.neon.server.model.Message
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsUnexpectedError
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageUnexpectedError
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.memberBlockContext
import com.thebrownfoxx.outcome.onFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

class WebSocketRemoteMessageDataSource(
    private val session: WebSocketSession,
    private val logger: Logger,
) : RemoteMessageDataSource {
    private val dataSourceScope = CoroutineScope(Dispatchers.Default) + SupervisorJob()

    private val conversationsCache =
        SingleCache<Outcome<List<Message>, DataOperationError>>(dataSourceScope)

    private val messageCache = Cache<MessageId, Outcome<Message, GetError>>(dataSourceScope)

    init {
        memberBlockContext("init") {
            session.subscribe<GetConversationPreviewsMemberNotFound> { response ->
                logger.logError(response, context)
            }
            session.subscribe<GetConversationPreviewsUnexpectedError> {
                conversationsCache.emit(Failure(DataOperationError.UnexpectedError))
            }
            session.subscribe<GetConversationPreviewsSuccessful> { response ->
                conversationsCache.emit(Success(response.conversations))
            }

            session.subscribe<GetMessageUnauthorized> { response ->
                logger.logError(response, context)
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
    }

    override val conversationPreviews = memberBlockContext("conversationPreviews") {
        conversationsCache.getAsFlow {
            session.send(GetConversationsRequest())
                .onFailure { conversationsCache.emit(Failure(DataOperationError.ConnectionError)) }
        }
    }

    override fun getMessageAsFlow(id: MessageId) = memberBlockContext("getMesssageAsFlow") {
        messageCache.getAsFlow(id) {
            session.send(GetMessageRequest(id))
                .onFailure { conversationsCache.emit(Failure(DataOperationError.ConnectionError)) }
        }
    }
}