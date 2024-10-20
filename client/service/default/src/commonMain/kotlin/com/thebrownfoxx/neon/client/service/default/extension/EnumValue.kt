package com.thebrownfoxx.neon.client.service.default.extension

inline fun <reified T: Enum<T>> enumValueOfOrNull(value: String?): T? = runCatching {
    enumValueOf<T>(value!!)
}.getOrNull()