package com.thebrownfoxx.neon.client.service

import com.thebrownfoxx.neon.client.service.authenticator.Authenticator
import com.thebrownfoxx.neon.client.service.jwt.TokenStorage

interface Dependencies {
    val tokenStorage: TokenStorage
    val authenticator: Authenticator
}