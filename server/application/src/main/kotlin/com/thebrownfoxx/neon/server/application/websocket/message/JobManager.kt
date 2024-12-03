package com.thebrownfoxx.neon.server.application.websocket.message

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class JobManager<in K>(private val coroutineScope: CoroutineScope) {
    private val jobs = ConcurrentHashMap<K, Job>()

    operator fun set(key: K, action: suspend () -> Unit) {
        mutableMapOf(1 to 2)[1] = 1
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