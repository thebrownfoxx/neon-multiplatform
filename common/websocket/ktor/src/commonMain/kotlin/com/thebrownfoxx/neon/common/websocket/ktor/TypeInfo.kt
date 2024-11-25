package com.thebrownfoxx.neon.common.websocket.ktor

import com.thebrownfoxx.neon.common.websocket.model.Type
import io.ktor.util.reflect.TypeInfo

fun Type.toKtorTypeInfo() = TypeInfo(kClass, kType)