package com.thebrownfoxx.neon.client.service

interface Dependencies {
    val authenticator: Authenticator
    val groupManager: GroupManager
    val memberManager: MemberManager
    val messenger: Messenger
}