package com.thebrownfoxx.neon.server.service.group.model

enum class SetInviteCodeError {
    Unauthorized,
    GroupNotFound,
    GroupNotCommunity,
    DuplicateInviteCode,
    InternalError,
}