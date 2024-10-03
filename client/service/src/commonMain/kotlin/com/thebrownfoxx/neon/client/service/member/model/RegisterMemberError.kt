package com.thebrownfoxx.neon.client.service.member.model

sealed interface RegisterMemberError {
    data object InvalidInviteCode : RegisterMemberError
    data class UsernameTooLong(val maxLength: Int) : RegisterMemberError
    data object UsernameNotAlphanumeric : RegisterMemberError
    data object UsernameTaken : RegisterMemberError
    data class PasswordTooShort(val minLength: Int) : RegisterMemberError
}