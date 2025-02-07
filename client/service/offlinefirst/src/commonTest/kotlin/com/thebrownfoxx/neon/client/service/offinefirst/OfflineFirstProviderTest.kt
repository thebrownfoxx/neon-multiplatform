package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.must.contentMustEqual
import com.thebrownfoxx.outcome.Success
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class OfflineFirstProviderTest {
    @Test
    fun localAndRemoteSucceededEqualValues_mustEmitSuccessOnce() = runNonTerminatingTest {
        offlineFirstProviderTest {
            emitLocal(Success(1))
            emitRemote(Success(1f))
        }.takeAsList(2) contentMustEqual listOf(Success(1))
    }

    @Test
    fun localAndRemoteSucceededOutdatedLocal_mustEmitLocalThenRemote() = runNonTerminatingTest {
        offlineFirstProviderTest {
            emitLocal(Success(1))
            emitRemote(Success(2f))
        }.takeAsList(2) contentMustEqual listOf(
            Success(1),
            Success(2),
        )
    }

    @Test
    fun localAndRemoteSucceededOutdatedRemote_mustEmitLocalIgnoreRemote() = runNonTerminatingTest {
        offlineFirstProviderTest {
            emitLocal(Success(2))
            emitRemote(Success(1f))
        }.takeAsList(2) contentMustEqual listOf(Success(2))
    }

    @Test
    fun localFailedRemoteSucceeded_mustEmitRemoteIgnoreLocal() = runNonTerminatingTest {
        offlineFirstProviderTest {
            emitLocal(UnitFailure)
            emitRemote(Success(1f))
        }.takeAsList(2) contentMustEqual listOf(Success(1))
    }

    @Test
    fun localSucceededRemoteFailed_mustEmitLocalThenRemote() = runNonTerminatingTest {
        offlineFirstProviderTest {
            emitLocal(Success(1))
            emitRemote(UnitFailure)
        }.takeAsList(2) contentMustEqual listOf(
            Success(1),
            UnitFailure,
        )
    }

    @Test
    fun localFailedRemoteFailed_mustEmitFailureOnce() = runNonTerminatingTest {
        offlineFirstProviderTest {
            emitLocal(UnitFailure)
            emitRemote(UnitFailure)
        }.takeAsList(2) contentMustEqual listOf(UnitFailure)
    }
}

private fun runNonTerminatingTest(block: suspend CoroutineScope.() -> Unit) {
    try {
        runTest {
            block()
            cancel()
        }
    } catch (_: CancellationException) {
        // Ignore cancellation
    }
}

private inline fun CoroutineScope.offlineFirstProviderTest(
    block: OfflineFirstTestScope.() -> Unit,
): Flow<IntOutcome> {
    val localFlow = MutableSharedFlow<IntOutcome>(replay = 1)
    val remoteFlow = MutableSharedFlow<FloatOutcome>(replay = 1)
    return OfflineFirstProvider(
        localFlow,
        remoteFlow,
        OfflineFirstTestHandler { localFlow.emit(it) },
        this,
    )
        .also { OfflineFirstTestScope(localFlow, remoteFlow).apply(block) }
        .getAsFlow()
}