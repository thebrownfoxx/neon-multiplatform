package com.thebrownfoxx.neon.common.type.id

interface Id {
    val uuid: Uuid
    val value get() = uuid.value
}