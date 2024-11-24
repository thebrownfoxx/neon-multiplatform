package com.thebrownfoxx.neon.common.type.id

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MessageIdSerializer::class)
data class MessageId(override val uuid: Uuid = Uuid()) : Id

object MessageIdSerializer : KSerializer<MessageId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("messageId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MessageId {
        return MessageId(Uuid(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: MessageId) {
        encoder.encodeString(value.value)
    }
}