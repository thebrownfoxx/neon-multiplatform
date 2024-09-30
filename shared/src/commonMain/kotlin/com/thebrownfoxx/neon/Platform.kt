package com.thebrownfoxx.neon

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform