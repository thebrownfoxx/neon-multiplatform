package com.thebrownfoxx.neon.server.application.websocket.message

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class JobManager<in K>(private val coroutineScope: CoroutineScope) {
    private val jobs = ConcurrentHashMap<K, Job>()

    operator fun set(key: K, action: suspend () -> Unit) {
        jobs[key]?.cancel()
        jobs[key] = coroutineScope.launch { action() }
    }
}

class SingleJobManager(private val coroutineScope: CoroutineScope) {
    private var job: Job? = null

    fun set(action: suspend () -> Unit) {
        job?.cancel()
        job = coroutineScope.launch { action() }
    }
}