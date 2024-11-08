package com.thebrownfoxx.neon.server.service.member.model

sealed interface GetMemberError {
    data object NotFound : GetMemberError
    data object ConnectionError : GetMemberError
}