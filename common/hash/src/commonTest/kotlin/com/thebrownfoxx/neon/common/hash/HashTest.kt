package com.thebrownfoxx.neon.common.hash

import com.thebrownfoxx.neon.must.mustBe
import com.thebrownfoxx.neon.must.mustBeTrue
import com.thebrownfoxx.neon.must.mustNotBe
import kotlin.test.Test

class HashTest : Hasher by MultiplatformHasher() {
    @Test
    fun hashGeneratesSameHashForSameStringAndSalt() {
        val string = "string"
        val salt = generateSalt()
        val hash1 = hash(string, salt)
        val hash2 = hash(string, salt)
        hash2 mustBe hash1
    }

    @Test
    fun hashGeneratesDifferentHashesForDifferentStringsWithSameSalt() {
        val string1 = "string1"
        val string2 = "string2"
        val salt = generateSalt()
        val hash1 = hash(string1, salt)
        val hash2 = hash(string2, salt)
        hash2 mustNotBe hash1
    }

    @Test
    fun hashGeneratesDifferentHashesForSameStringWithDifferentSalts() {
        val string = "string"
        val hash1 = hash(string, generateSalt())
        val hash2 = hash(string, generateSalt())
        hash2 mustNotBe hash1
    }

    @Test
    fun matchesReturnsTrueIfHashesPlaintextMatch() {
        val string = "string"
        val hash = hash(string, generateSalt())
        (string matches hash).mustBeTrue()
    }

    @Test
    fun matchesReturnsFalseIfHashesPlaintextDoNotMatch() {
        val string = "string"
        val hash = hash(string, generateSalt())
        ("hello" doesNotMatch hash).mustBeTrue()
    }

    @Test
    fun doesNotMatchIsInverseOfMatches() {
        val string = "string"
        val hash = hash(string, generateSalt())
        (string matches hash).mustBeTrue()
        ("hello" doesNotMatch hash).mustBeTrue()
    }
}