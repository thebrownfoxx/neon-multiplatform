package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.common.websocket.listen
import com.thebrownfoxx.neon.common.websocket.model.RequestId
import com.thebrownfoxx.neon.common.websocket.send
import com.thebrownfoxx.neon.server.application.websocket.KtorServerWebSocketSession
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsSuccessful
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
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.SendMessageUnexpectedError
import com.thebrownfoxx.neon.server.service.Messenger
import com.thebrownfoxx.neon.server.service.Messenger.GetConversationPreviewsError
import com.thebrownfoxx.neon.server.service.Messenger.GetMessageError
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.supervisorScope

class MessageWebSocketMessageManager private constructor(
    private val session: KtorServerWebSocketSession,
    private val messenger: Messenger,
) {
    companion object {
        suspend fun startListening(
            session: KtorServerWebSocketSession,
            messenger: Messenger,
        ) = MessageWebSocketMessageManager(session, messenger).apply { startListening() }
    }

    private lateinit var getConversationPreviewsJobManager: SingleJobManager
    private lateinit var getMessagesJobManager: JobManager<GroupId>
    private lateinit var getMessageJobManager: JobManager<MessageId>
    private lateinit var sendMessageJobManager: JobManager<RequestId>

    private suspend fun startListening() {
        supervisorScope {
            getConversationPreviewsJobManager = SingleJobManager(this)
            getMessagesJobManager = JobManager(this)
            getMessageJobManager = JobManager(this)
            sendMessageJobManager = JobManager(this)

            session.listen<GetConversationPreviewsRequest>(this) {
                getConversationPreviews()
            }

            session.listen<GetMessagesRequest>(this) { request ->
                getMessages(request.groupId)
            }

            session.listen<GetMessageRequest>(this) { request ->
                getMessage(request.id)
            }

            session.listen<SendMessageRequest>(this) { request ->
                sendMessage(request.requestId, request.id, request.groupId, request.content)
            }
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

    private fun getMessages(groupId: GroupId) {
        getMessagesJobManager[groupId] = {
            messenger.getMessages(session.memberId, groupId).collect { messagesOutcome ->
                messagesOutcome.onSuccess { messages ->
                    session.send(GetMessagesSuccessful(groupId, messages))
                }.onFailure { error ->
                    when (error) {
                        Messenger.GetMessagesError.Unauthorized ->
                            session.send(GetMessagesUnauthorized(groupId, session.memberId))

                        Messenger.GetMessagesError.GroupNotFound ->
                            session.send(GetMessagesGroupNotFound(groupId))

                        Messenger.GetMessagesError.UnexpectedError ->
                            session.send(GetMessagesUnexpectedError(groupId))
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

    private fun sendMessage(
        requestId: RequestId,
        id: MessageId,
        groupId: GroupId,
        content: String,
    ) {
        sendMessageJobManager[requestId] = {
            messenger.sendMessage(id, session.memberId, groupId, content).onSuccess {
                session.send(SendMessageSuccessful(requestId, id))
            }.onFailure { error ->
                when (error) {
                    Messenger.SendMessageError.Unauthorized ->
                        session.send(SendMessageUnauthorized(requestId, id, session.memberId))

                    Messenger.SendMessageError.GroupNotFound ->
                        session.send(SendMessageGroupNotFound(requestId, id, groupId))

                    Messenger.SendMessageError.UnexpectedError ->
                        session.send(SendMessageUnexpectedError(requestId, id))
                }
            }
        }
    }
}