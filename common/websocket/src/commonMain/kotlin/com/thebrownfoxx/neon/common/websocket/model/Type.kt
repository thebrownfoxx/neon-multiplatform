package com.thebrownfoxx.neon.common.websocket.model

import com.thebrownfoxx.outcome.getOrNull
import com.thebrownfoxx.outcome.runFailing
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class Type(
    val kClass: KClass<*>,
    val kType: KType? = null,
)

inline fun <reified T> typeOf(): Type = Type(T::class, kTypeOfOrNull<T>())

@PublishedApi
internal inline fun <reified T> kTypeOfOrNull(): KType? = runFailing { typeOf<T>() }.getOrNull()