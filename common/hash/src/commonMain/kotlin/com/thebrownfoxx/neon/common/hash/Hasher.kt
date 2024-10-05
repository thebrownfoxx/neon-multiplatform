package com.thebrownfoxx.neon.common.hash

interface Hasher {
    fun generateSalt(): ByteArray
    fun hash(plaintext: String, salt: ByteArray = generateSalt()): Hash
    infix fun String.matches(hash: Hash): Boolean
    infix fun String.doesNotMatch(hash: Hash) = !matches(hash)
}