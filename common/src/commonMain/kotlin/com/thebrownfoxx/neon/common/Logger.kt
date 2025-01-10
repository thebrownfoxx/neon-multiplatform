package com.thebrownfoxx.neon.common

import com.thebrownfoxx.outcome.StackTrace

interface Logger {
    fun logInfo(message: Any?, stackTrace: StackTrace = StackTrace())
    fun logError(message: Any?, stackTrace: StackTrace = StackTrace())
    fun logDebug(message: Any?, stackTrace: StackTrace = StackTrace())
}

object PrintLogger : Logger {
    override fun logInfo(message: Any?, stackTrace: StackTrace) {
        println("INFO: $message ${stackTrace.label}")
    }

    override fun logError(message: Any?, stackTrace: StackTrace) {
        println("ERROR: $message ${stackTrace.label}")
    }

    override fun logDebug(message: Any?, stackTrace: StackTrace) {
        println("DEBUG: $message ${stackTrace.label}")
    }
}