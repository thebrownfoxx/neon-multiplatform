package com.thebrownfoxx.neon.client.service.group.model

sealed interface GetGroupError {
    data object NotFound : GetGroupError
    data object ConnectionError : GetGroupError
}