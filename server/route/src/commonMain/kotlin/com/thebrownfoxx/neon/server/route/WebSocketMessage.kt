package com.thebrownfoxx.neon.server.route

import com.thebrownfoxx.neon.common.type.Id
import com.thebrownfoxx.neon.common.type.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
open class WebSocketMessage(
    val label: String,
    @Suppress("unused") val description: String,
)

@Serializable(with = WebSocketMessageIdSerializer::class)
data class WebSocketMessageId(override val uuid: Uuid = Uuid()) : Id

object WebSocketMessageIdSerializer : KSerializer<WebSocketMessageId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("memberId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): WebSocketMessageId {
        return WebSocketMessageId(Uuid(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: WebSocketMessageId) {
        encoder.encodeString(value.value)
    }
}