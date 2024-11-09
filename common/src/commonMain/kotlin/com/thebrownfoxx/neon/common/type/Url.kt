package com.thebrownfoxx.neon.common.type

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = UrlSerializer::class)
data class Url(val value: String)

fun String.asUrl() = Url(this)

object UrlSerializer : KSerializer<Url> {
    override val descriptor = PrimitiveSerialDescriptor("url", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Url {
        return decoder.decodeString().asUrl()
    }

    override fun serialize(encoder: Encoder, value: Url) {
        encoder.encodeString(value.value)
    }
}