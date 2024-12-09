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
import kotlin.reflect.KClass

// Careful. Because of some weirdness with Kotlin Serialization, we are deserializing the
// WebSocketMessageHeader from JSON that are WebSocketMessage instances as a workaround.
// Since there is currently nothing linking WebSocketMessageHeader and WebSocketMessage,
// Be sure that the two are always in sync.

@Serializable
abstract class WebSocketMessage(
    @Suppress("unused") val label: WebSocketMessageLabel,
    val description: String? = null,
) {
    abstract val requestId: RequestId?

    constructor(
        kClass: KClass<out Any>,
        description: String? = null,
    ) : this(
        label = WebSocketMessageLabel(kClass),
        description = description,
    )
}

@Serializable
data class WebSocketMessageHeader(
    val label: WebSocketMessageLabel,
    val description: String? = null,
    val requestId: RequestId? = null,
)

@Serializable(with = WebSocketMessageLabelSerializer::class)
data class WebSocketMessageLabel(val value: String) {
    constructor(kClass: KClass<out Any>) : this(kClass.simpleName ?: "Unknown")
}

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

@Serializable(with = RequestIdSerializer::class)
data class RequestId(override val uuid: Uuid = Uuid()) : Id

object RequestIdSerializer : KSerializer<RequestId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("requestId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): RequestId {
        return RequestId(Uuid(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: RequestId) {
        encoder.encodeString(value.value)
    }
}