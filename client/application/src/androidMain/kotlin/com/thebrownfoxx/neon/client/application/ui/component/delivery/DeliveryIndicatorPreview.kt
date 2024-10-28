package com.thebrownfoxx.neon.client.application.ui.component.delivery

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun SentPreview() {
    NeonTheme {
        DeliveryIndicator(
            delivery = DeliveryState.Sent,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun DeliveredPreview() {
    NeonTheme {
        DeliveryIndicator(
            delivery = DeliveryState.Delivered,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun ReadPreview() {
    NeonTheme {
        DeliveryIndicator(
            delivery = DeliveryState.Read,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun ReactedPreview() {
    NeonTheme {
        DeliveryIndicator(
            delivery = DeliveryState.Reacted("😆"),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun FailedPreview() {
    NeonTheme {
        DeliveryIndicator(
            delivery = DeliveryState.Failed,
            modifier = Modifier.padding(16.dp),
        )
    }
}