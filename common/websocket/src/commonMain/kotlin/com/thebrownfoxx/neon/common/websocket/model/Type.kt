package com.thebrownfoxx.neon.common.websocket.model

import com.thebrownfoxx.outcome.blockContext
import com.thebrownfoxx.outcome.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class Type(
    val kClass: KClass<*>,
    val kType: KType? = null,
)

inline fun <reified T> typeOf(): Type = Type(T::class, kTypeOfOrNull<T>())

@PublishedApi
internal inline fun <reified T> kTypeOfOrNull(): KType? = blockContext("kTypeOrNull") {
    runFailing { typeOf<T>() }.getOrNull()
}