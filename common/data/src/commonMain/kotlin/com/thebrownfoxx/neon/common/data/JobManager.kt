package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

// TODO: Handle memory leak from completed coroutines

class JobManager<in K>(private val externalScope: CoroutineScope) {
    private val jobs = ConcurrentHashMap<K, Job>()

    operator fun set(key: K, action: suspend () -> Unit) {
        jobs[key]?.cancel()
        jobs[key] = externalScope.launch { action() }
    }
}

class SingleJobManager(private val externalScope: CoroutineScope) {
    private var job: Job? = null

    fun set(action: suspend () -> Unit) {
        job?.cancel()
        job = externalScope.launch { action() }
    }
}