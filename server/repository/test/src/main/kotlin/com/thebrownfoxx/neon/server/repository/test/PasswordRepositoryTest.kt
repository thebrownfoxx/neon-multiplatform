package com.thebrownfoxx.neon.server.repository.test

import com.thebrownfoxx.neon.common.hash.Hasher
import com.thebrownfoxx.neon.common.hash.MultiplatformHasher
import com.thebrownfoxx.neon.common.model.Failure
import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Success
import com.thebrownfoxx.neon.common.model.unitSuccess
import com.thebrownfoxx.neon.must.mustBe
import com.thebrownfoxx.neon.must.mustBeTrue
import com.thebrownfoxx.neon.server.repository.password.PasswordRepository
import com.thebrownfoxx.neon.server.repository.password.model.RepositoryGetPasswordHashError
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

abstract class PasswordRepositoryTest : Hasher by MultiplatformHasher() {
    val passwords = listOf(
        MemberId() to "carlos sainz",
        MemberId() to "lando norris",
    )

    private lateinit var passwordRepository: PasswordRepository

    abstract fun createPasswordRepository(): PasswordRepository

    @BeforeTest
    fun setup() {
        runTest {
            passwordRepository = createPasswordRepository()

            for ((memberId, password) in passwords) {
                passwordRepository.setHash(memberId, hash(password))
            }
        }
    }

    @Test
    fun getHashShouldReturnPasswordHash() {
        runTest {
            for ((memberId, password) in passwords) {
                val actualHashResult = passwordRepository.getHash(memberId)
                (actualHashResult is Success &&
                        password matches actualHashResult.value).mustBeTrue()
            }
        }
    }

    @Test
    fun getShouldReturnNotFoundIfPasswordHashDoesNotExist() {
        runTest {
            val actualHashResult = passwordRepository.getHash(MemberId())
            actualHashResult mustBe Failure(RepositoryGetPasswordHashError.NotFound)
        }
    }

    @Test
    fun setShouldSetPasswordHash() {
        runTest {
            val memberId = MemberId()
            val password = "charles leclerc"

            val setResult = passwordRepository.setHash(memberId, hash(password))
            setResult mustBe unitSuccess()

            val actualHashResult = passwordRepository.getHash(memberId)
            (actualHashResult is Success &&
                    password matches actualHashResult.value).mustBeTrue()
        }
    }
}