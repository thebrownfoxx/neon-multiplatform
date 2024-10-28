package com.thebrownfoxx.neon.client.application.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun NoProgressPreview() {
    NeonTheme {
        InformationCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            InformationCardIconText(
                icon = Icons.TwoTone.Info,
                iconContentDescription = null,
                text = "Did you know that skibidi wah pow pow",
            )
        }
    }
}

@Preview
@Composable
private fun ProgressPreview() {
    NeonTheme {
        InformationCard(
            progressIndicator = {
                LinearProgressIndicator(
                    progress = { 0.8f },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(text = "Rizzmaxxing...")
        }
    }
}