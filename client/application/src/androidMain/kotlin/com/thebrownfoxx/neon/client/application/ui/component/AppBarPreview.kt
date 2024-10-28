package com.thebrownfoxx.neon.client.application.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun TopBarPreview() {
    NeonTheme {
        TopBarScrim {
            AppBar(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Top bar",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun BottomBarPreview() {
    NeonTheme {
        BottomBarScrim {
            AppBar(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Bottom bar",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}