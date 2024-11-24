package com.thebrownfoxx.neon.common.type

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = JwtSerializer::class)
data class Jwt(val value: String)

object JwtSerializer : KSerializer<Jwt> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("jwt", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Jwt {
        return Jwt(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Jwt) {
        encoder.encodeString(value.value)
    }
}