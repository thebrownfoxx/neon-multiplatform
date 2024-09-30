package com.thebrownfoxx.neon.common.type

sealed interface Loadable<out T> {
    fun <R> map(transform: (T) -> R): Loadable<R>
}

data object Loading : Loadable<Nothing> {
    override fun <R> map(transform: (Nothing) -> R): Loadable<R> = Loading
}

data class Loaded<out T>(val value: T) : Loadable<T> {
    override fun <R> map(transform: (T) -> R): Loadable<R> = Loaded(transform(value))
}

fun <T, R> List<Loadable<T>>.combine(transform: (List<T>) -> R): Loadable<R> = when {
    all { it is Loaded } -> Loaded(transform(map { (it as Loaded).value }))
    else -> Loading
}

fun <T, R> Array<Loadable<T>>.combine(transform: (List<T>) -> R): Loadable<R> = when {
    all { it is Loaded } -> Loaded(transform(map { (it as Loaded).value }))
    else -> Loading
}