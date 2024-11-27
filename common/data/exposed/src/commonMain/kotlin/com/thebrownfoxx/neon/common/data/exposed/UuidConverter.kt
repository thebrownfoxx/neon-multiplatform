package com.thebrownfoxx.neon.common.data.exposed

import com.thebrownfoxx.neon.common.type.id.Id
import com.thebrownfoxx.neon.common.type.id.Uuid
import java.util.UUID

fun Uuid.toJavaUuid(): UUID = UUID.fromString(value)

fun Id.toJavaUuid(): UUID = UUID.fromString(value)

fun UUID.toCommonUuid() = Uuid(toString())