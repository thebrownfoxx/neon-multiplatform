package com.thebrownfoxx.neon.client.service.offinefirst.old

internal fun <T> OfflineFirstScope<T>.transformLazyRemote(
    localNotFound: (T) -> Boolean,
    remoteSucceeded: (T) -> Boolean,
    remoteFailedUnexpectedly: (T) -> Boolean,
    updateLocal: suspend (T) -> Unit,
) {
    transformRemote { previousLocal, newRemote ->
        when {
            remoteSucceeded(newRemote) -> updateLocal(newRemote)
            remoteFailedUnexpectedly(newRemote) && previousLocal !=  null &&
                    localNotFound(previousLocal) -> emit()
        }
    }
}

internal fun <T> OfflineFirstScope<T>.transformLazy(
    localSucceeded: (T) -> Boolean,
    localNotFound: (T) -> Boolean,
    localFailedUnexpectedly: (T) -> Boolean,
    remoteSucceeded: (T) -> Boolean,
    remoteNotFound: (T) -> Boolean,
    remoteFailedUnexpectedly: (T) -> Boolean,
    updateLocal: suspend (T) -> Unit,
) {
    defaultTransformLocal(
        localSucceeded = localSucceeded,
        localNotFound = localNotFound,
        localFailedUnexpectedly = localFailedUnexpectedly,
        remoteNotFound = remoteNotFound,
    )
    transformLazyRemote(
        localNotFound = localNotFound,
        remoteSucceeded = remoteSucceeded,
        remoteFailedUnexpectedly = remoteFailedUnexpectedly,
        updateLocal = updateLocal,
    )
}

internal fun <T> OfflineFirstScope<T>.transformLazy(
    succeeded: (T) -> Boolean,
    notFound: (T) -> Boolean,
    failedUnexpectedly: (T) -> Boolean,
    updateLocal: suspend (T) -> Unit,
) {
    defaultTransformLocal(
        succeeded = succeeded,
        notFound = notFound,
        failedUnexpectedly = failedUnexpectedly,
    )
    transformLazyRemote(
        localNotFound = notFound,
        remoteSucceeded = succeeded,
        remoteFailedUnexpectedly = failedUnexpectedly,
        updateLocal = updateLocal,
    )
}