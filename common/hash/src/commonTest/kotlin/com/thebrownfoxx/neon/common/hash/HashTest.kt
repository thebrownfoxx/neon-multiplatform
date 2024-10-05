package com.thebrownfoxx.neon.common.hash

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class HashTest : Hasher by MultiplatformHasher() {
    @Test
    fun hashGeneratesSameHashForSameStringAndSalt() {
        val string = "string"
        val salt = generateSalt()
        val hash1 = hash(string, salt)
        val hash2 = hash(string, salt)
        assertEquals(hash1, hash2)
    }

    @Test
    fun hashGeneratesDifferentHashesForDifferentStringsWithSameSalt() {
        val string1 = "string1"
        val string2 = "string2"
        val salt = generateSalt()
        val hash1 = hash(string1, salt)
        val hash2 = hash(string2, salt)
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun hashGeneratesDifferentHashesForSameStringWithDifferentSalts() {
        val string = "string"
        val hash1 = hash(string, generateSalt())
        val hash2 = hash(string, generateSalt())
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun matchesReturnsTrueIfHashesPlaintextMatch() {
        val string = "string"
        val hash = hash(string, generateSalt())
        assertTrue(string matches hash)
    }

    @Test
    fun matchesReturnsFalseIfHashesPlaintextDoNotMatch() {
        val string = "string"
        val hash = hash(string, generateSalt())
        assertTrue("hello" doesNotMatch hash)
    }

    @Test
    fun doesNotMatchIsInverseOfMatches() {
        val string = "string"
        val hash = hash(string, generateSalt())
        assertTrue(string matches hash) // Infix notation
        assertTrue("hello" doesNotMatch hash)
    }
}