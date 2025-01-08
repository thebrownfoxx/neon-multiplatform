package com.thebrownfoxx.neon.common.data.websocket.ktor

import com.thebrownfoxx.neon.common.type.Type
import io.ktor.util.reflect.TypeInfo

fun Type.toKtorTypeInfo() = TypeInfo(kClass, kType)