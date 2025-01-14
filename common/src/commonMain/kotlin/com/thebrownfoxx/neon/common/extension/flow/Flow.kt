package com.thebrownfoxx.neon.common.extension.flow

import kotlinx.coroutines.flow.flowOf

fun <T> T.flow() = flowOf(this)