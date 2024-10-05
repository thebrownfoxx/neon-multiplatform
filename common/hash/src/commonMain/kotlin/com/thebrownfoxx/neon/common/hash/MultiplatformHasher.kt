package com.thebrownfoxx.neon.common.hash

import org.kotlincrypto.SecureRandom
import org.kotlincrypto.hash.sha2.SHA512

class MultiplatformHasher : Hasher {
    private val algorithm = SHA512()

    override fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = random.nextBytesOf(16)
        return salt
    }

    override fun hash(plaintext: String, salt: ByteArray): Hash {
        val digest = algorithm.digest(plaintext.encodeToByteArray() + salt)
        return Hash(digest, salt)
    }

    override fun String.matches(hash: Hash): Boolean {
        val stringHash = hash(this, hash.salt)
        return stringHash == hash
    }
}