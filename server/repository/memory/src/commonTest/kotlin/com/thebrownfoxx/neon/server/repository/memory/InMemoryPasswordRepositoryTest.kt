package com.thebrownfoxx.neon.server.repository.memory

import com.thebrownfoxx.neon.server.repository.password.PasswordRepository
import com.thebrownfoxx.neon.server.repository.test.PasswordRepositoryTest

@Suppress("unused")
class InMemoryPasswordRepositoryTest : PasswordRepositoryTest() {
    override fun createPasswordRepository(): PasswordRepository {
        return InMemoryPasswordRepository()
    }
}