package com.thebrownfoxx.neon.server.application.websocket.message

import com.thebrownfoxx.neon.common.outcome.onFailure
import com.thebrownfoxx.neon.common.outcome.onSuccess
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MessageId
import com.thebrownfoxx.neon.server.application.websocket.KtorServerWebSocketSession
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewGroupNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewInternalError
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewUnauthorized
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationPreviewsSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationsInternalError
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationsMemberNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationsRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetConversationsSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageInternalError
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageNotFound
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageRequest
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageSuccessful
import com.thebrownfoxx.neon.server.route.websocket.message.GetMessageUnauthorized
import com.thebrownfoxx.neon.server.service.messenger.Messenger
import com.thebrownfoxx.neon.server.service.messenger.model.GetConversationPreviewError
import com.thebrownfoxx.neon.server.service.messenger.model.GetConversationPreviewsError
import com.thebrownfoxx.neon.server.service.messenger.model.GetConversationsError
import com.thebrownfoxx.neon.server.service.messenger.model.GetMessageError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

class MessageWebSocketMessageManager(
    private val session: KtorServerWebSocketSession,
    private val messenger: Messenger,
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    private val getConversationsJobManager = SingleJobManager(coroutineScope, session.close)
    private val getMessageJobManager = JobManager<MessageId>(coroutineScope, session.close)
    private val getConversationPreviewsJobManager = SingleJobManager(coroutineScope, session.close)
    private val getConversationPreviewJobManager =
        JobManager<GroupId>(coroutineScope, session.close)

    init {
        session.subscribe<GetConversationsRequest> {
            getConversations()
        }
        session.subscribe<GetConversationPreviewRequest> { request ->
            getConversationPreview(request.groupId)
        }
        session.subscribe<GetConversationPreviewsRequest> {
            getConversationPreviews()
        }
        session.subscribe<GetMessageRequest> { request ->
            getMessage(request.id)
        }
    }

    private fun getConversations() {
        getConversationsJobManager.set {
            messenger.getConversations(session.memberId).collect { conversationsOutcome ->
                conversationsOutcome.onSuccess { conversations ->
                    session.send(GetConversationsSuccessful(conversations))
                }.onFailure { error ->
                    when (error) {
                        GetConversationsError.MemberNotFound ->
                            session.send(GetConversationsMemberNotFound(session.memberId))

                        GetConversationsError.InternalError ->
                            session.send(GetConversationsInternalError(session.memberId))
                    }
                }
            }
        }
    }

    private fun getConversationPreview(groupId: GroupId) {
        getConversationPreviewJobManager[groupId] = {
            messenger.getConversationPreview(session.memberId, groupId).collect { previewOutcome ->
                previewOutcome.onSuccess { preview ->
                    session.send(GetConversationPreviewSuccessful(groupId, preview))
                }.onFailure { error ->
                    when (error) {
                        GetConversationPreviewError.Unauthorized ->
                            session
                                .send(GetConversationPreviewUnauthorized(groupId, session.memberId))

                        GetConversationPreviewError.GroupNotFound ->
                            session.send(GetConversationPreviewGroupNotFound(groupId))

                        GetConversationPreviewError.InternalError ->
                            session.send(GetConversationPreviewInternalError(groupId))
                    }
                }
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
                            session.send(GetConversationsMemberNotFound(session.memberId))

                        GetConversationPreviewsError.InternalError ->
                            session.send(GetConversationsInternalError(session.memberId))
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

                        GetMessageError.InternalError ->
                            session.send(GetMessageInternalError(id))
                    }
                }
            }
        }
    }
}