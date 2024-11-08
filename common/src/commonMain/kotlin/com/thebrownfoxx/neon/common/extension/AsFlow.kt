package com.thebrownfoxx.neon.common.extension

import kotlinx.coroutines.flow.flowOf

fun <T> T.asFlow() = flowOf(this)