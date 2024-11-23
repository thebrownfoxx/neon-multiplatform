package com.thebrownfoxx.neon.server.route.group

import com.thebrownfoxx.neon.common.model.GroupId
import io.ktor.resources.Resource

@Resource("/group")
class GroupRoute {
    @Resource("{id}")
    class Get(val parent: GroupRoute = GroupRoute(), val id: GroupId)
}