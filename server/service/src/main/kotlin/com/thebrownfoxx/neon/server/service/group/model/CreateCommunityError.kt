package com.thebrownfoxx.neon.server.service.group.model

sealed interface CreateCommunityError {
    data object Unauthorized : CreateCommunityError
    data class NameTooLong(val maxLength: Int) : CreateCommunityError
    data object ConnectionError : CreateCommunityError
}