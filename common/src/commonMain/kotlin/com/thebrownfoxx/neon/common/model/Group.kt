package com.thebrownfoxx.neon.common.model

import com.thebrownfoxx.neon.common.type.Id
import com.thebrownfoxx.neon.common.type.IgnoredUuid
import com.thebrownfoxx.neon.common.type.Url
import com.thebrownfoxx.neon.common.type.Uuid
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class ChatGroup(
    override val id: GroupId = GroupId(),
) : Group {
    override fun ignoreId() = copy(id = ignoredGroupId)
}

@Serializable
data class Community(
    override val id: GroupId = GroupId(),
    val name: String,
    val avatarUrl: Url?,
    val god: Boolean,
) : Group {
    override fun ignoreId() = copy(id = ignoredGroupId)
}

sealed interface Group {
    val id: GroupId

    fun ignoreId(): Group
}

private val ignoredGroupId = GroupId(IgnoredUuid)

@Serializable(with = GroupIdSerializer::class)
data class GroupId(override val uuid: Uuid = Uuid()) : Id

object GroupIdSerializer : KSerializer<GroupId> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("groupId", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): GroupId {
        return GroupId(Uuid(decoder.decodeString()))
    }

    override fun serialize(encoder: Encoder, value: GroupId) {
        return encoder.encodeString(value.value)
    }
}
