package com.thebrownfoxx.neon.client.application.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
private fun Preview() {
    WrapText(
        "Box around text with a very very very very longlonglonglongwordsefuefuweyfwefweuyfwegfuweugfuewgfuewgufgweugfyuwegfyuwegyfgu wefyewfyu wgeugf",
        color = Color.White,
        modifier = Modifier
            .border(width = 2.dp, color = Color.Red)
            .background(Color.DarkGray)
    )
}