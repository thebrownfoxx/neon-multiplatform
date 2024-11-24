package com.thebrownfoxx.neon.common.type.id

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = GroupIdSerializer::class)
data class GroupId(override val uuid: Uuid = Uuid()) : Id

object GroupIdSerializer : KSerializer<GroupId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("groupId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): GroupId {
        return GroupId(Uuid(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: GroupId) {
        encoder.encodeString(value.value)
    }
}