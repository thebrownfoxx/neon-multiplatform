package com.thebrownfoxx.neon.common

import com.thebrownfoxx.outcome.StackTrace
import com.thebrownfoxx.outcome.map.FailureMapScope

interface Logger {
    fun logInfo(message: Any?, stackTrace: StackTrace = StackTrace())
    fun logError(message: Any?, stackTrace: StackTrace = StackTrace())
    fun logDebug(message: Any?, stackTrace: StackTrace = StackTrace())
}

object LoggerProvider {
    var logger: Logger = PrintLogger
}

fun logInfo(message: Any?, stackTrace: StackTrace = StackTrace()) {
    LoggerProvider.logger.logInfo(message, stackTrace)
}

fun logError(message: Any?, stackTrace: StackTrace = StackTrace()) {
    LoggerProvider.logger.logError(message, stackTrace)
}

fun logDebug(message: Any?, stackTrace: StackTrace = StackTrace()) {
    LoggerProvider.logger.logDebug(message, stackTrace)
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

fun FailureMapScope.logError() {
    logError(log)
}