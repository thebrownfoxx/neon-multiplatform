package com.thebrownfoxx.neon.ui.extension

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class Corner {
    TopStart,
    TopEnd,
    BottomEnd,
    BottomStart;

    companion object {
        val All = entries
        val None = setOf<Corner>()
    }
}

object Side {
    val Start = setOf(Corner.TopStart, Corner.BottomStart)
    val Top = setOf(Corner.TopStart, Corner.TopEnd)
    val End = setOf(Corner.TopEnd, Corner.BottomEnd)
    val Bottom = setOf(Corner.BottomEnd, Corner.BottomStart)
}

data class RoundedCorners(val corners: Set<Corner>) {
    companion object {
        val None = RoundedCorners()
        val All = RoundedCorners(*Corner.All.toTypedArray())

        val DefaultRoundRadius = 8.dp
        val DefaultNonRoundRadius = 4.dp
    }

    constructor(vararg corners: Corner) : this(corners.toSet())

    operator fun contains(corner: Corner) = corners.contains(corner)

    @Composable
    fun toShape(
        roundRadius: Dp = DefaultRoundRadius,
        nonRoundRadius: Dp = DefaultNonRoundRadius,
    ): RoundedCornerShape {
        val topStartRadius by animateDpAsState(
            targetValue = if (corners.contains(Corner.TopStart)) roundRadius else nonRoundRadius,
            label = "topStartRadius",
        )

        val topEndRadius by animateDpAsState(
            targetValue = if (corners.contains(Corner.TopEnd)) roundRadius else nonRoundRadius,
            label = "topEndRadius",
        )

        val bottomEndRadius by animateDpAsState(
            targetValue = if (corners.contains(Corner.BottomEnd)) roundRadius else nonRoundRadius,
            label = "bottomEndRadius",
        )

        val bottomStartRadius by animateDpAsState(
            targetValue = if (corners.contains(Corner.BottomStart)) roundRadius else nonRoundRadius,
            label = "bottomStartRadius",
        )

        return RoundedCornerShape(
            topStart = topStartRadius,
            topEnd = topEndRadius,
            bottomEnd = bottomEndRadius,
            bottomStart = bottomStartRadius,
        )
    }
}