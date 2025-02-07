package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class OfflineFirstTestScope(
    private val localFlow: MutableSharedFlow<IntOutcome>,
    private val remoteFlow: MutableSharedFlow<FloatOutcome>,
) {
    suspend fun emitLocal(local: IntOutcome) = localFlow.emit(local)
    suspend fun emitRemote(remote: FloatOutcome) {
        delay(100.milliseconds)
        remoteFlow.emit(remote)
    }
}

fun interface OfflineFirstTestHandler : OfflineFirstHandler<IntOutcome, FloatOutcome> {
    suspend fun updateLocal(newLocal: IntOutcome)

    override fun hasLocalFailed(local: IntOutcome): Boolean {
        return local is Failure
    }

    override suspend fun updateLocal(newRemote: FloatOutcome, oldLocal: IntOutcome) {
        when (newRemote) {
            is Failure -> onRemoteFailure(oldLocal)
            is Success -> onRemoteSuccess(newRemote, oldLocal)
        }
    }

    private suspend fun onRemoteFailure(oldLocal: IntOutcome) {
        when (oldLocal) {
            is Failure -> { /* Do nothing */ }
            is Success -> updateLocal(UnitFailure)
        }
    }

    private suspend fun onRemoteSuccess(
        newRemote: Success<Float>,
        oldLocal: IntOutcome,
    ) {
        when (oldLocal) {
            is Failure -> updateLocal(newRemote.toIntOutcome())
            is Success ->
                if (newRemote.value > oldLocal.value) updateLocal(newRemote.toIntOutcome())
        }
    }

    private fun FloatOutcome.toIntOutcome() = map { it.toInt() }
}

typealias IntOutcome = Outcome<Int, Unit>
typealias FloatOutcome = Outcome<Float, Unit>
val UnitFailure = Failure(Unit)

suspend fun <T> Flow<T>.takeAsList(
    count: Int,
    timeout: Duration = 100.milliseconds,
): List<T> {
    val list = mutableListOf<T>()
    withTimeoutOrNull(timeout) {
        take(count).collect { list.add(it) }
    }
    return list.toList()
}