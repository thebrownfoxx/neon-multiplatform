package com.thebrownfoxx.neon.common.type

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid as KUuid

internal val IgnoredUuid = Uuid("IGNORED")

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Uuid(val value: String = KUuid.random().toString()) {
    override fun toString(): String = value
}