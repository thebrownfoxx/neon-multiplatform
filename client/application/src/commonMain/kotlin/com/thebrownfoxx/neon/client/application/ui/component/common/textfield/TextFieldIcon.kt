package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.component.common.AnimatedVisibility
import com.thebrownfoxx.neon.client.application.ui.component.common.ExpandAxis

@Composable
fun TextFieldIcon(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    content: @Composable () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        expandAxis = ExpandAxis.Horizontal,
    ) {
        Box(
            modifier = Modifier.sizeIn(
                minWidth = TextFieldDefaults.MinHeight,
                minHeight = TextFieldDefaults.MinHeight,
            ),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}