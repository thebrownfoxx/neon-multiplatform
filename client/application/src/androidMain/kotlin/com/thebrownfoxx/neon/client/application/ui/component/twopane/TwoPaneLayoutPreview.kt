package com.thebrownfoxx.neon.client.application.ui.component.twopane

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.client.application.ui.extension.Side
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
private fun Preview() {
    NeonTheme {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            TwoPaneLayout(
                leftPane = {
                    Pane(roundedCorners = RoundedCorners(Side.Start)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                        ) {
                            Text(
                                text = "Left Pane",
                                style = MaterialTheme.typography.headlineLarge,
                            )
                        }
                    }
                },
                rightPane = {
                    Pane(roundedCorners = RoundedCorners(Side.End)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                        ) {
                            Text(
                                text = "Right Pane",
                                style = MaterialTheme.typography.headlineLarge,
                            )
                        }
                    }
                },
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}