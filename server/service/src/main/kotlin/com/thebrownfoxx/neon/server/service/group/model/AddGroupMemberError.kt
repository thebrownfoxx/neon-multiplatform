package com.thebrownfoxx.neon.server.service.group.model

enum class AddGroupMemberError {
    Unauthorized,
    AlreadyAMember,
    GroupNotFound,
    MemberNotFound,
    ConnectionError,
}