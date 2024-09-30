package com.thebrownfoxx.neon.common.type

import kotlinx.serialization.Serializable

@Serializable
data class Url(val value: String)

fun String.asUrl() = Url(this)