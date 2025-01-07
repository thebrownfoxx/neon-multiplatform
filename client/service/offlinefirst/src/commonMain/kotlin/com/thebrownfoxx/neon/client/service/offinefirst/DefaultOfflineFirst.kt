package com.thebrownfoxx.neon.client.service.offinefirst

fun <T> OfflineFirstScope<T>.defaultTransformLocal(
    localSucceeded: (T) -> Boolean,
    localNotFound: (T) -> Boolean,
    localFailedUnexpectedly: (T) -> Boolean,
    remoteSucceeded: (T) -> Boolean,
    remoteNotFound: (T) -> Boolean,
    remoteFailedUnexpectedly: (T) -> Boolean,
) {
    transformLocal { newLocal, previousRemote ->
        if ((localSucceeded(newLocal) && (previousRemote == null ||
                    remoteSucceeded(previousRemote) ||
                    remoteFailedUnexpectedly(previousRemote))) ||
            (localNotFound(newLocal) && previousRemote != null &&
                    remoteNotFound(previousRemote)) ||
            localFailedUnexpectedly(newLocal)
        ) emit()
    }
}

fun <T> OfflineFirstScope<T>.defaultTransformRemote(
    localNotFound: (T) -> Boolean,
    remoteSucceeded: (T) -> Boolean,
    remoteNotFound: (T) -> Boolean,
    remoteFailedUnexpectedly: (T) -> Boolean,
    updateLocal: suspend (T) -> Unit,
    deleteLocal: suspend () -> Unit,
) {
    transformRemote { previousLocal, newRemote ->
        when {
            remoteSucceeded(newRemote) -> updateLocal(newRemote)
            remoteNotFound(newRemote) -> deleteLocal()
            remoteFailedUnexpectedly(newRemote) && previousLocal != null &&
                    localNotFound(previousLocal) -> emit()
        }
    }
}

fun <T> OfflineFirstScope<T>.defaultTransform(
    localSucceeded: (T) -> Boolean,
    localNotFound: (T) -> Boolean,
    localFailedUnexpectedly: (T) -> Boolean,
    remoteSucceeded: (T) -> Boolean,
    remoteNotFound: (T) -> Boolean,
    remoteFailedUnexpectedly: (T) -> Boolean,
    updateLocal: suspend (T) -> Unit,
    deleteLocal: suspend () -> Unit,
) {
    defaultTransformLocal(
        localSucceeded = localSucceeded,
        localNotFound = localNotFound,
        localFailedUnexpectedly = localFailedUnexpectedly,
        remoteSucceeded = remoteSucceeded,
        remoteNotFound = remoteNotFound,
        remoteFailedUnexpectedly = remoteFailedUnexpectedly,
    )
    defaultTransformRemote(
        localNotFound = localNotFound,
        remoteSucceeded = remoteSucceeded,
        remoteNotFound = remoteNotFound,
        remoteFailedUnexpectedly = remoteFailedUnexpectedly,
        updateLocal = updateLocal,
        deleteLocal = deleteLocal,
    )
}

fun <T> OfflineFirstScope<T>.defaultTransformLocal(
    succeeded: (T) -> Boolean,
    notFound: (T) -> Boolean,
    failedUnexpectedly: (T) -> Boolean,
) {
    defaultTransformLocal(
        localSucceeded = succeeded,
        localNotFound = notFound,
        localFailedUnexpectedly = failedUnexpectedly,
        remoteSucceeded = succeeded,
        remoteNotFound = notFound,
        remoteFailedUnexpectedly = failedUnexpectedly,
    )
}

fun <T> OfflineFirstScope<T>.defaultTransformRemote(
    succeeded: (T) -> Boolean,
    notFound: (T) -> Boolean,
    failedUnexpectedly: (T) -> Boolean,
    updateLocal: suspend (T) -> Unit,
    deleteLocal: suspend () -> Unit,
) {
    transformRemote { previousLocal, newRemote ->
        when {
            succeeded(newRemote) -> updateLocal(newRemote)
            notFound(newRemote) -> deleteLocal()
            failedUnexpectedly(newRemote) && previousLocal != null &&
                    notFound(previousLocal) -> emit()
        }
    }
}

fun <T> OfflineFirstScope<T>.defaultTransform(
    succeeded: (T) -> Boolean,
    notFound: (T) -> Boolean,
    failedUnexpectedly: (T) -> Boolean,
    updateLocal: suspend (T) -> Unit,
    deleteLocal: suspend () -> Unit,
) {
    defaultTransform(
        localSucceeded = succeeded,
        localNotFound = notFound,
        localFailedUnexpectedly = failedUnexpectedly,
        remoteSucceeded = succeeded,
        remoteNotFound = notFound,
        remoteFailedUnexpectedly = failedUnexpectedly,
        updateLocal = updateLocal,
        deleteLocal = deleteLocal,
    )
}