package com.thebrownfoxx.neon.common.websocket.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
open class WebSocketMessage(
    val label: WebSocketMessageLabel,
    @Suppress("unused") val description: String? = null,
)

@Serializable(with = WebSocketMessageLabelSerializer::class)
data class WebSocketMessageLabel(val value: String)

object WebSocketMessageLabelSerializer : KSerializer<WebSocketMessageLabel> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("memberId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): WebSocketMessageLabel {
        return WebSocketMessageLabel(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: WebSocketMessageLabel) {
        encoder.encodeString(value.value)
    }
}