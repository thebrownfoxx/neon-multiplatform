package com.thebrownfoxx.neon.common.extension

class FlatList<out E>(private val subLists: List<List<E>>) : AbstractList<E>() {
    private val cumulativeSizes = subLists.runningFold(0) { accumulator, subList ->
        accumulator + subList.size
    }

    override val size = subLists.sumOf { it.size }

    override operator fun get(index: Int): E {
        val listIndex = cumulativeSizes.indexOfFirst { it > index } - 1
        val subListIndex = index - cumulativeSizes.getOrElse(listIndex) { 0 }
        return subLists[listIndex][subListIndex]
    }
}

fun <E> flatListOf(vararg subLists: List<E>) = FlatList(subLists.toList())