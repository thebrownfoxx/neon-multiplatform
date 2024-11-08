package com.thebrownfoxx.neon.client.service.default

import com.thebrownfoxx.neon.client.service.Dependencies
import io.ktor.client.HttpClient

class DefaultDependencies(httpClient: HttpClient) : Dependencies {
    override val tokenStorage = InMemoryTokenStorage()

    override val authenticator = RemoteAuthenticator(
        httpClient = httpClient,
        tokenStorage = tokenStorage,
    )
}