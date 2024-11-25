package com.thebrownfoxx.neon.common.websocket.model

import com.thebrownfoxx.neon.common.type.id.Id
import com.thebrownfoxx.neon.common.type.id.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = WebSocketMessageIdSerializer::class)
data class WebSocketAcknowledgementId(override val uuid: Uuid = Uuid()) : Id

object WebSocketMessageIdSerializer : KSerializer<WebSocketAcknowledgementId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("memberId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): WebSocketAcknowledgementId {
        return WebSocketAcknowledgementId(Uuid(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: WebSocketAcknowledgementId) {
        encoder.encodeString(value.value)
    }
}