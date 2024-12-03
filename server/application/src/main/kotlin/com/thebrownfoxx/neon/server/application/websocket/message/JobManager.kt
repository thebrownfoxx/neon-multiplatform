package com.thebrownfoxx.neon.server.application.websocket.message

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class JobManager<in K>(private val coroutineScope: CoroutineScope) {
    private val jobs = ConcurrentHashMap<K, Job>()

    operator fun set(key: K, action: suspend () -> Unit) {
        jobs[key]?.cancel()
        jobs[key] = coroutineScope.launch {
            action()
        }
    }

    fun cancelAll() {
        for (job in jobs.values) {
            job.cancel()
        }
    }
}

fun <K> JobManager(
    coroutineScope: CoroutineScope,
    cancelAll: Flow<Unit>,
): JobManager<K> = JobManager<K>(coroutineScope).apply {
    coroutineScope.launch {
        cancelAll.collect {
            cancelAll()
        }
    }
}

class SingleJobManager(private val coroutineScope: CoroutineScope) {
    private var job: Job? = null

    fun set(action: suspend () -> Unit) {
        job?.cancel()
        job = coroutineScope.launch {
            action()
        }
    }

    fun cancel() {
        job?.cancel()
    }
}

fun SingleJobManager(
    coroutineScope: CoroutineScope,
    cancel: Flow<Unit>,
) = SingleJobManager(coroutineScope).apply {
    coroutineScope.launch {
        cancel.collect {
            cancel()
        }
    }
}