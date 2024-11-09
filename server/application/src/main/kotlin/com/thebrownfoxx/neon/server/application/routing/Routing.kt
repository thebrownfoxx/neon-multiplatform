package com.thebrownfoxx.neon.server.application.routing

import com.thebrownfoxx.neon.server.application.routing.authentication.login
import com.thebrownfoxx.neon.server.application.routing.group.getGroup
import io.ktor.resources.Resource
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import io.ktor.server.resources.get
import io.ktor.server.response.respondText
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    install(Resources)

    install(ContentNegotiation) {
        json()
    }
    routing {
        get<HelloWorld> { call.respondText("Hello, world!") }
        login()
        getGroup()
    }
}

@Resource("/hello-world")
class HelloWorld