package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.flow.MutableSharedFlow

fun <V> cacheFlow() = MutableSharedFlow<V>(replay = 1)