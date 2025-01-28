package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.must.contentMustEqual
import com.thebrownfoxx.outcome.Failure
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class OfflineFirstFlowTest {
    @Test
    fun localAndRemoteSucceededEqualValues_mustEmitSuccessOnce() = runTest {
        offlineFirstTest {
            emitLocal(Success(1))
            emitRemote(Success(1f))
        }.takeAsList(2) contentMustEqual listOf(Success(1))
    }

    @Test
    fun localAndRemoteSucceededOutdatedLocal_mustEmitLocalThenRemote() = runTest {
        offlineFirstTest {
            emitLocal(Success(1))
            emitRemote(Success(2f))
        }.takeAsList(2) contentMustEqual listOf(
            Success(1),
            Success(2),
        )
    }

    @Test
    fun localAndRemoteSucceededOutdatedRemote_mustEmitLocalIgnoreRemote() = runTest {
        offlineFirstTest {
            emitLocal(Success(2))
            emitRemote(Success(1f))
        }.takeAsList(2) contentMustEqual listOf(Success(2))
    }

    @Test
    fun localFailedRemoteSucceeded_mustEmitRemoteIgnoreLocal() = runTest {
        offlineFirstTest {
            emitLocal(UnitFailure)
            emitRemote(Success(1f))
        }.takeAsList(2) contentMustEqual listOf(Success(1))
    }

    @Test
    fun localSucceededRemoteFailed_mustEmitLocalThenRemote() = runTest {
        offlineFirstTest {
            emitLocal(Success(1))
            emitRemote(UnitFailure)
        }.takeAsList(2) contentMustEqual listOf(
            Success(1),
            UnitFailure,
        )
    }

    @Test
    fun localFailedRemoteFailed_mustEmitFailureOnce() = runTest {
        offlineFirstTest {
            emitLocal(UnitFailure)
            emitRemote(UnitFailure)
        }.takeAsList(2) contentMustEqual listOf(UnitFailure)
    }
}

inline fun offlineFirstTest(block: OfflineFirstTestScope.() -> Unit): Flow<IntOutcome> {
    val localFlow = MutableSharedFlow<IntOutcome>(replay = 1)
    val remoteFlow = MutableSharedFlow<FloatOutcome>(replay = 1)
    return offlineFirstFlow(
        localFlow,
        remoteFlow,
        OfflineFirstTestHandler { localFlow.emit(it) },
    ).also { OfflineFirstTestScope(localFlow, remoteFlow).apply(block) }
}

class OfflineFirstTestScope(
    private val localFlow: MutableSharedFlow<IntOutcome>,
    private val remoteFlow: MutableSharedFlow<FloatOutcome>,
) {
    suspend fun emitLocal(local: IntOutcome) = localFlow.emit(local)
    suspend fun emitRemote(remote: FloatOutcome) = remoteFlow.emit(remote)
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

private typealias IntOutcome = Outcome<Int, Unit>
private typealias FloatOutcome = Outcome<Float, Unit>
private val UnitFailure = Failure(Unit)

private suspend fun <T> Flow<T>.takeAsList(
    count: Int,
    timeout: Duration = 100.milliseconds,
): List<T> {
    val list = mutableListOf<T>()
    withTimeoutOrNull(timeout) {
        take(count).collect { list.add(it) }
    }
    return list.toList()
}