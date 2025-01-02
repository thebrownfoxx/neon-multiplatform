package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

fun <T, E> offlineFirst(
    localFlow: Flow<Outcome<T, *>>,
    remoteFlow: Flow<Outcome<T, E>>,
    updateLocal: suspend (T) -> Unit,
): Flow<Outcome<T, E>> {
    return channelFlow {
        launch {
            localFlow.collectLatest { outcome ->
                outcome.onSuccess { send(Success(it)) }
            }
        }
        launch {
            remoteFlow.collectLatest { outcome ->
                outcome
                    .onFailure { send(Failure(it)) }
                    .onSuccess { updateLocal(it) }
            }
        }
    }
}