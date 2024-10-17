package com.thebrownfoxx.neon.client.application.ui.component.loader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TextLoader(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Loader { color ->
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = modifier.width(IntrinsicSize.Max),
        ) {
            // This is just so it has the same height as in the actual list item
            Text(
                text = text,
                maxLines = 1,
                style = style,
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {},
            )
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .background(color)
                    .drawWithContent {},
            )
        }
    }
}

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