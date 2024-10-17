package com.thebrownfoxx.neon.ui.component.loader

import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.common.type.Loadable
import com.thebrownfoxx.neon.common.type.Loaded
import com.thebrownfoxx.neon.common.type.Loading
import com.thebrownfoxx.neon.ui.extension.loaderContentTransition

@Composable
fun <T> LoadableSwapper(
    loadable: Loadable<T>,
    loader: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit,
) {
    AnimatedContent(
        targetState = loadable,
        label = "contentLoad",
        contentKey = { it::class },
        transitionSpec = { loaderContentTransition() },
        modifier = modifier,
    ) { targetLoadable ->
        when (targetLoadable) {
            is Loading -> loader()
            is Loaded -> content(targetLoadable.value)
        }
    }
}