package com.thebrownfoxx.neon.ui.component.common.textfield

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Man
import androidx.compose.material3.Icon
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.ui.component.common.ExpandAxis
import com.thebrownfoxx.neon.ui.component.common.ThemedAnimatedVisibility
import com.thebrownfoxx.neon.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ThemedTextFieldIcon(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    content: @Composable () -> Unit,
) {
    ThemedAnimatedVisibility(
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

@Composable
fun ThemedTextFieldLeadingIcon(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    content: @Composable () -> Unit,
) {
    ThemedTextFieldIcon(
        modifier = modifier,
        visible = visible,
        content = content,
    )
}

@Composable
fun ThemedTextFieldTrailingIcon(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    content: @Composable () -> Unit,
) {
    ThemedTextFieldIcon(
        modifier = modifier,
        visible = visible,
        content = content,
    )
}

@Preview
@Composable
private fun LeadingPreview() {
    NeonTheme {
        ThemedTextFieldLeadingIcon {
            Icon(imageVector = Icons.TwoTone.Man, contentDescription = null)
        }
    }
}

@Preview
@Composable
private fun TrailingPreview() {
    NeonTheme {
        ThemedTextFieldTrailingIcon {
            Icon(imageVector = Icons.TwoTone.Man, contentDescription = null)
        }
    }
}