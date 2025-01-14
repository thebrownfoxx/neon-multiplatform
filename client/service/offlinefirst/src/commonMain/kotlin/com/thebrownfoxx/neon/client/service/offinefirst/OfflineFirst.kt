package com.thebrownfoxx.neon.client.service.offinefirst

import com.thebrownfoxx.neon.common.extension.flow.mergeTransform
import kotlinx.coroutines.flow.Flow

fun <T> offlineFirst(
    localFlow: Flow<T>,
    remoteFlow: Flow<T>,
    transform: OfflineFirstScope<T>.() -> Unit,
): Flow<T> {
    val offlineFirstScope = OfflineFirstScope<T>().apply(transform)

    var local: T? = null
    var remote: T? = null

    @Suppress("RemoveExplicitTypeArguments") // Kotlin's type inference is broken
    return mergeTransform<T, T, T>(
        leftFlow = localFlow,
        rightFlow = remoteFlow,
        transformLeft = { newLocal ->
            val transformScope = TransformScope { emit(newLocal) }
            local = newLocal
            offlineFirstScope.localTransform?.invoke(transformScope, newLocal, remote)
        },
        transformRight = { newRemote ->
            val transformScope = TransformScope { emit(newRemote) }
            remote = newRemote
            offlineFirstScope.remoteTransform?.invoke(transformScope, local, newRemote)
        },
    )
}

class OfflineFirstScope<T> {
    // TODO: This will be accessible to its users, unfortunately.
    //  I hate Kotlin's lack of visibility modifiers
    internal var localTransform: LocalTransform<T>? = null
    internal var remoteTransform: RemoteTransform<T>? = null

    fun transformLocal(transform: LocalTransform<T>) {
        localTransform = transform
    }

    fun transformRemote(transform: RemoteTransform<T>) {
        remoteTransform = transform
    }
}

typealias LocalTransform<T> = suspend TransformScope.(newLocal: T, previousRemote: T?) -> Unit
typealias RemoteTransform<T> = suspend TransformScope.(previousLocal: T?, newRemote: T) -> Unit

fun interface TransformScope {
    suspend fun emit()
}