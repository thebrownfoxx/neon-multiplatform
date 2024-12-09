package com.thebrownfoxx.neon.common.data.transaction

class TransactionContext {
    private val reversibles = ArrayDeque<Reversible<*>>()

    fun <T> Reversible<T>.register(): T {
        reversibles.addFirst(this)
        return result
    }

    suspend fun reverseAll() {
        for (reversible in reversibles) {
            reversible.reverse()
        }
    }

    suspend fun finalizeAll() {
        for (reversible in reversibles) {
            reversible.finalize()
        }
    }
}