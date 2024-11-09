package com.thebrownfoxx.neon.server.application.routing.group

import com.thebrownfoxx.neon.common.model.ChatGroup
import com.thebrownfoxx.neon.common.model.Community
import com.thebrownfoxx.neon.common.model.getOrElse
import com.thebrownfoxx.neon.server.application.dependency.DependencyProvider
import com.thebrownfoxx.neon.server.application.plugin.AuthenticationType
import com.thebrownfoxx.neon.server.application.plugin.authenticate
import com.thebrownfoxx.neon.server.model.group.GetGroupResponse
import com.thebrownfoxx.neon.server.model.group.GroupRoute
import com.thebrownfoxx.neon.server.service.group.model.GetGroupError
import io.ktor.http.HttpStatusCode
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import kotlinx.coroutines.flow.first

fun Route.getGroup() {
    with(DependencyProvider.dependencies) {
        authenticate(AuthenticationType.Jwt) {
            get<GroupRoute.Get> { request ->
                val groupFlow = groupManager.getGroup(request.id)
                // TODO: Register this Flow to WebSocket updater
                val group = groupFlow.first().getOrElse { error ->
                    return@get when (error) {
                        is GetGroupError.NotFound -> call.respond(
                            HttpStatusCode.NotFound,
                            GetGroupResponse.NotFound(),
                        )
                        GetGroupError.ConnectionError -> call.respond(
                            HttpStatusCode.InternalServerError,
                            GetGroupResponse.ConnectionError(),
                        )
                    }
                }

                when (group) {
                    is ChatGroup -> call.respond(
                        HttpStatusCode.OK,
                        GetGroupResponse.SuccessfulChatGroup(group),
                    )
                    is Community -> call.respond(
                        HttpStatusCode.OK,
                        GetGroupResponse.SuccessfulCommunity(group),
                    )
                }
            }
        }
    }
}