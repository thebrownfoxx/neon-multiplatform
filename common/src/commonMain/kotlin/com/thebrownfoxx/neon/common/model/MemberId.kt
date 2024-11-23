package com.thebrownfoxx.neon.common.model

import com.thebrownfoxx.neon.common.type.Id
import com.thebrownfoxx.neon.common.type.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MemberIdSerializer::class)
data class MemberId(override val uuid: Uuid = Uuid()) : Id

object MemberIdSerializer : KSerializer<MemberId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("memberId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): MemberId {
        return MemberId(Uuid(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: MemberId) {
        encoder.encodeString(value.value)
    }
}
