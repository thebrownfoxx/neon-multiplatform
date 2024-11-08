package com.thebrownfoxx.neon.common.model

import com.thebrownfoxx.neon.common.type.Id
import com.thebrownfoxx.neon.common.type.Uuid
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Message(
    val id: MessageId = MessageId(),
    val groupId: GroupId,
    val senderId: MemberId,
    val content: String,
    val timestamp: Instant,
    val delivery: Delivery,
) {
    fun ignoreId(): Message = copy(id = ignoredMessageId)
}

private val ignoredMessageId = MessageId(Uuid("IGNORED"))

@Serializable(with = MessageIdSerializer::class)
data class MessageId(override val uuid: Uuid = Uuid()) : Id

object MessageIdSerializer : KSerializer<MessageId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("messageId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MessageId {
        return MessageId(Uuid(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: MessageId) {
        return encoder.encodeString(value.value)
    }
}

// TODO: Maybe server delivery must be different from client delivery (thus also differentiating the whole model?)
sealed interface Delivery {
    data object Sending: Delivery
    data object Sent : Delivery
    data object Delivered : Delivery
    data object Read : Delivery
    data object Failed : Delivery
}
