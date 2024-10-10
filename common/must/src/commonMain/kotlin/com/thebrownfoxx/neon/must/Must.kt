package com.thebrownfoxx.neon.must

import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertIsNot
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

infix fun <T> T.mustBe(expected: T) = assertEquals(expected, this)

infix fun <T> T.mustNotBe(expected: T) = assertNotEquals(expected, this)

infix fun <T> Iterable<T>.contentMustEqual(expected: Iterable<T>) = assertContentEquals(expected, this)

infix fun <T> Iterable<T>.mustContain(expected: T) = assertContains(this, expected)

fun Boolean.mustBeTrue() = assertTrue(this)

fun Boolean.mustBeFalse() = assertFalse(this)

inline fun <reified T> Any?.mustBeA() = assertIs<T>(this)

inline fun <reified T> Any?.mustNotBeA() = assertIsNot<T>(this)