package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.server.repository.test.PasswordRepositoryTest
import com.thebrownfoxx.neon.server.repository.memory.InMemoryPasswordRepository
import com.thebrownfoxx.neon.server.repository.password.PasswordRepository

@Suppress("unused")
class InMemoryPasswordRepositoryTest : PasswordRepositoryTest() {
    override fun createPasswordRepository(): PasswordRepository {
        return InMemoryPasswordRepository()
    }
}