package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.common.WrapText
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners

@Composable
fun MessageBubble(
    content: String,
    roundedCorners: RoundedCorners,
    containerColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = contentColorFor(containerColor),
    drawText: Boolean = true,
) {
    val textMeasurer = rememberTextMeasurer()

    val textStyle = MaterialTheme.typography.bodyLarge

    val textLayoutResult = remember(content) {
        textMeasurer.measure(
            text = content,
            style = textStyle,
            maxLines = 1,
        )
    }

    val verticalPadding = 8.dp
    val horizontalPadding = verticalPadding * 2

    val circleRadius = with(LocalDensity.current) {
        textLayoutResult.size.height.toDp() / 2 + verticalPadding
    }

    val shape = roundedCorners.toShape(
        roundRadius = circleRadius,
        nonRoundRadius = 2.dp,
    )

    Surface(
        modifier = modifier,
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        WrapText(
            text = content,
            style = textStyle,
            modifier = Modifier
                .drawWithContent { if (drawText) drawContent() }
                .padding(
                    vertical = verticalPadding,
                    horizontal = horizontalPadding,
                ),
        )
    }
}