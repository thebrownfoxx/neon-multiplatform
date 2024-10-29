package com.thebrownfoxx.neon.client.application.ui.screen.login.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.app_name_lowercase
import org.jetbrains.compose.resources.stringResource

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