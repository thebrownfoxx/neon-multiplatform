package com.thebrownfoxx.neon.common

import com.thebrownfoxx.outcome.StackTrace

interface Logger {
    fun logInfo(message: Any, stackTrace: StackTrace = StackTrace())
    fun logError(message: Any, stackTrace: StackTrace = StackTrace())
    fun logDebug(message: Any, stackTrace: StackTrace = StackTrace())
}

object PrintLogger : Logger {
    override fun logInfo(message: Any, stackTrace: StackTrace) {
        println("INFO: $message $stackTrace")
    }

    override fun logError(message: Any, stackTrace: StackTrace) {
        println("ERROR: $message $stackTrace")
    }

    override fun logDebug(message: Any, stackTrace: StackTrace) {
        println("DEBUG: $message $stackTrace")
    }
}