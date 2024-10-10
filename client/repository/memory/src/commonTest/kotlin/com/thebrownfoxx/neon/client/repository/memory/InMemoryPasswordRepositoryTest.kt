package com.thebrownfoxx.neon.client.repository.memory

import com.thebrownfoxx.neon.client.repository.password.PasswordRepository
import com.thebrownfoxx.neon.client.repository.test.PasswordRepositoryTest

@Suppress("unused")
class InMemoryPasswordRepositoryTest : PasswordRepositoryTest() {
    override fun createPasswordRepository(): PasswordRepository {
        return InMemoryPasswordRepository()
    }
}