package com.thebrownfoxx.neon.common.data

import kotlinx.coroutines.flow.MutableSharedFlow

fun <V> cacheSharedFlow() = MutableSharedFlow<V>(replay = 1, extraBufferCapacity = 16)