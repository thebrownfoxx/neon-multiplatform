package com.thebrownfoxx.neon.client.application.ui.screen.login.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.app_name_lowercase
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Wordmark(modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            val appName = stringResource(Res.string.app_name_lowercase)
            withStyle(
                style = SpanStyle(color = MaterialTheme.colorScheme.tertiary),
            ) {
                append(appName.first())
            }
            withStyle(
                style = SpanStyle(color = MaterialTheme.colorScheme.primary),
            ) {
                append(appName.substring(1))
            }
        },
        style = MaterialTheme.typography.displayLarge,
        fontSize = 128.sp,
        lineHeight = 20.sp,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        Wordmark(modifier = Modifier.padding(16.dp))
    }
}