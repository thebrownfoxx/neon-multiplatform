package com.thebrownfoxx.neon.server.dependency

import com.thebrownfoxx.neon.common.hash.MultiplatformHasher
import com.thebrownfoxx.neon.server.repository.group.GroupRepository
import com.thebrownfoxx.neon.server.repository.memory.InMemoryGroupMemberRepository
import com.thebrownfoxx.neon.server.repository.memory.InMemoryGroupRepository
import com.thebrownfoxx.neon.server.repository.memory.InMemoryInviteCodeRepository
import com.thebrownfoxx.neon.server.repository.memory.InMemoryMemberRepository
import com.thebrownfoxx.neon.server.repository.memory.InMemoryPasswordRepository
import com.thebrownfoxx.neon.server.service.group.GroupManager
import com.thebrownfoxx.neon.server.service.jwt.JwtProcessor
import com.thebrownfoxx.neon.server.service.jwt.model.JwtConfig
import com.thebrownfoxx.neon.server.service.member.MemberManager
import com.thebrownfoxx.neon.server.service.repository.DefaultGroupManager
import com.thebrownfoxx.neon.server.service.repository.DefaultJwtProcessor
import com.thebrownfoxx.neon.server.service.repository.DefaultMemberManager
import com.thebrownfoxx.neon.server.service.repository.DefaultPermissionChecker
import kotlin.time.Duration.Companion.days

class DefaultDependencies : Dependencies {
    private val jwtConfig =
        JwtConfig(
            realm = "neon",
            issuer = "https://neon/",
            audience = "neon-audience",
            validity = 30.days,
            secret = "secret", // TODO: This should be moved to env
        )

    override val jwtProcessor = DefaultJwtProcessor(jwtConfig)

    private val groupRepository = InMemoryGroupRepository()
    private val memberRepository = InMemoryMemberRepository()
    private val groupMemberRepository = InMemoryGroupMemberRepository()
    private val inviteCodeRepository = InMemoryInviteCodeRepository()
    private val passwordRepository = InMemoryPasswordRepository()
    private val hasher = MultiplatformHasher()

    private val permissionChecker = DefaultPermissionChecker(groupMemberRepository)

    override val groupManager = DefaultGroupManager(
        permissionChecker,
        groupRepository,
        memberRepository,
        groupMemberRepository,
        inviteCodeRepository,
    )

    override val memberManager = DefaultMemberManager(
        memberRepository,
        passwordRepository,
        inviteCodeRepository,
        groupMemberRepository,
        hasher,
    )
}