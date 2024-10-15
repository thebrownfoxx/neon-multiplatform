package com.thebrownfoxx.neon.server.service.member.model

sealed interface RegisterMemberError {
    data class InvalidInviteCode(val inviteCode: String) : RegisterMemberError
    data class UsernameTooLong(val username: String, val maxLength: Int) : RegisterMemberError
    data class UsernameNotAlphanumeric(val username: String) : RegisterMemberError
    data class UsernameTaken(val username: String) : RegisterMemberError
    data class PasswordTooShort(val minLength: Int) : RegisterMemberError
    data object ConnectionError: RegisterMemberError
}