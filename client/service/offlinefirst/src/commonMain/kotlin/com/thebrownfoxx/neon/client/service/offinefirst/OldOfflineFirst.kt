package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.common.extension.flow.mergeTransform
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.onFailure
import com.thebrownfoxx.outcome.map.onSuccess
import kotlinx.coroutines.flow.Flow

@Deprecated("Use offlineFirst or mergeTransform")
fun <T, E> oldOfflineFirst(
    localFlow: Flow<Outcome<T, *>>,
    remoteFlow: Flow<Outcome<T, E>>,
    updateLocal: suspend (T) -> Unit,
): Flow<Outcome<T, E>> {
    return mergeTransform(
        leftFlow = localFlow,
        rightFlow = remoteFlow,
        transformLeft = { local ->
            local.onSuccess { emit(Success(it)) }
        },
        transformRight = { remote ->
            remote
                .onFailure { emit(Failure(it)) }
                .onSuccess { updateLocal(it) }
        }
    )
}