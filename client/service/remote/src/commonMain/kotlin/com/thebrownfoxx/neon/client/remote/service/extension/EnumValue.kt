package com.thebrownfoxx.neon.client.remote.service.extension

inline fun <reified T: Enum<T>> enumValueOfOrNull(value: String?): T? = runCatching {
    enumValueOf<T>(value!!)
}.getOrNull()