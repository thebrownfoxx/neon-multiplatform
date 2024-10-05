package com.thebrownfoxx.neon.common.hash

data class Hash(
    val value: ByteArray,
    val salt: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Hash

        if (!value.contentEquals(other.value)) return false
        if (!salt.contentEquals(other.salt)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        return result
    }
}