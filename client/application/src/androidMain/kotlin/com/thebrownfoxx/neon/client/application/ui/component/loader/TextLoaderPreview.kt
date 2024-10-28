package com.thebrownfoxx.neon.client.application.ui.component.loader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        TextLoader(
            text = "The quick brown fox!!!!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun SizeComparisonPreview() {
    NeonTheme {
        Text(
            text = "The quick brown fox!!!!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Red.copy(alpha = 0.2f)),
        )
        TextLoader(
            text = "The quick brown fox!!!!",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(16.dp)
                .background(Color.Yellow.copy(alpha = 0.2f)),
        )
    }
}