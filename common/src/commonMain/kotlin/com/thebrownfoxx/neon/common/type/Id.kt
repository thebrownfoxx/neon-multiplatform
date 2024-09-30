package com.thebrownfoxx.neon.common.type

interface Id {
    val uuid: Uuid
    val value get() = uuid.value
}