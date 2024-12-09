package com.thebrownfoxx.neon.common

import com.thebrownfoxx.outcome.BlockContext

interface Logger {
    fun logInfo(message: Any, context: BlockContext)
    fun logError(message: Any, context: BlockContext)
    fun logDebug(message: Any, context: BlockContext)
}

object PrintLogger : Logger {
    override fun logInfo(message: Any, context: BlockContext) {
        println("INFO: $message")
    }

    override fun logError(message: Any, context: BlockContext) {
        println("ERROR: $message")
    }

    override fun logDebug(message: Any, context: BlockContext) {
        println("DEBUG: $message")
    }
}