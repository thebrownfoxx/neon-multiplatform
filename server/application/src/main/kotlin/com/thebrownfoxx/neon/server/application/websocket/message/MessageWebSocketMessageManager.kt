package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.application.websocket.KtorServerWebSocketSession
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageUnexpectedError
import com.thebrownfoxx.neon.server.service.Messenger
import com.thebrownfoxx.neon.server.service.Messenger.GetConversationPreviewsError
import com.thebrownfoxx.neon.server.service.Messenger.GetMessageError
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

class MessageWebSocketMessageManager(
    private val session: KtorServerWebSocketSession,
    private val messenger: Messenger,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    private val getMessageJobManager = JobManager<MessageId>(coroutineScope, session.close)
    private val getConversationPreviewsJobManager = SingleJobManager(coroutineScope, session.close)

    init {
        session.subscribe<GetConversationPreviewsRequest> {
            getConversationPreviews()
        }
        session.subscribe<GetMessageRequest> { request ->
            getMessage(request.id)
        }
    }

    private fun getConversationPreviews() {
        getConversationPreviewsJobManager.set {
            messenger.getConversationPreviews(session.memberId).collect { conversationsOutcome ->
                conversationsOutcome.onSuccess { conversations ->
                    session.send(GetConversationPreviewsSuccessful(conversations))
                }.onFailure { error ->
                    when (error) {
                        GetConversationPreviewsError.MemberNotFound ->
                            session.send(GetConversationPreviewsMemberNotFound(session.memberId))

                        GetConversationPreviewsError.UnexpectedError ->
                            session.send(GetConversationPreviewsMemberNotFound(session.memberId))
                    }
                }
            }
        }
    }

    private fun getMessage(id: MessageId) {
        getMessageJobManager[id] = {
            messenger.getMessage(session.memberId, id).collect { messageOutcome ->
                messageOutcome.onSuccess { message ->
                    session.send(GetMessageSuccessful(message))
                }.onFailure { error ->
                    when (error) {
                        GetMessageError.Unauthorized ->
                            session.send(GetMessageUnauthorized(id, session.memberId))

                        GetMessageError.NotFound -> session.send(GetMessageNotFound(id))

                        GetMessageError.UnexpectedError ->
                            session.send(GetMessageUnexpectedError(id))
                    }
                }
            }
        }
    }
}