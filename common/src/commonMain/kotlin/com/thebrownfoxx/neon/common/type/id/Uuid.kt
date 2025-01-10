package com.thebrownfoxx.neon.common.type.id

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid as KUuid

val IgnoredUuid = Uuid("IGNORED")

@JvmInline
@OptIn(ExperimentalUuidApi::class)
@Serializable
value class Uuid(val value: String = KUuid.random().toString()) {
    override fun toString(): String = value
}