package com.thebrownfoxx.neon.client.application.ui.component.twopane

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.Spacer

@Composable
fun TwoPaneLayout(
    leftPane: @Composable () -> Unit,
    rightPane: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    center: Float = 0.5f,
) {
    Row(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxWidth(center),
            propagateMinConstraints = true,
        ) {
            leftPane()
        }
        Spacer(width = 16.dp)
        Box(
            modifier = Modifier.fillMaxWidth(),
            propagateMinConstraints = true,
        ) {
            rightPane()
        }
    }
}