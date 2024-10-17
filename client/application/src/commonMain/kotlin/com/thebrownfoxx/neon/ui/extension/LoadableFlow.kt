package com.thebrownfoxx.neon.ui.extension

import com.thebrownfoxx.neon.common.type.Loadable
import com.thebrownfoxx.neon.common.type.Loaded
import com.thebrownfoxx.neon.common.type.Loading
import com.thebrownfoxx.neon.common.type.combine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
@ExperimentalCoroutinesApi
fun <T, R> Flow<Loadable<T>>.mapLatestLoadable(
    @BuilderInference transform: (value: T) -> R,
): Flow<Loadable<R>> = mapLatest { loadable ->
    loadable.map(transform)
}

@OptIn(ExperimentalTypeInference::class)
@ExperimentalCoroutinesApi
inline fun <T, R> Flow<Loadable<T>>.flatMapLatestLoadable(
    @BuilderInference crossinline transform: suspend (value: T) -> Flow<Loadable<R>>,
): Flow<Loadable<R>> = flatMapLatest { loadable ->
    when (loadable) {
        is Loading -> flowOf(Loading)
        is Loaded -> transform(loadable.value)
    }
}

@OptIn(ExperimentalTypeInference::class)
@ExperimentalCoroutinesApi
inline fun <T, R> Flow<Loadable<T>>?.flatMapLatestNullableLoadable(
    @BuilderInference crossinline transform: suspend (value: T?) -> Flow<Loadable<R>>,
): Flow<Loadable<R>> = (this ?: flowOf(null)).flatMapLatest { loadable ->
    when (loadable) {
        null -> flowOf(Loading)
        is Loading -> flowOf(Loading)
        is Loaded -> transform(loadable.value)
    }
}

fun <T, R> combineLoadable(
    flows: Iterable<Flow<Loadable<T>>>,
    transform: (List<T>) -> R
): Flow<Loadable<R>> = combine(flows) { loadables ->
    loadables.combine(transform)
}