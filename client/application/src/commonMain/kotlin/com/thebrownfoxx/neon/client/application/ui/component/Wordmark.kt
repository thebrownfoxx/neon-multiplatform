package com.thebrownfoxx.neon.client.application.ui.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.extension.LocalInDarkTheme
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.app_name
import neon.client.application.generated.resources.hard_neon
import neon.client.application.generated.resources.hard_neon_dark
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun Wordmark(modifier: Modifier = Modifier) {
    val drawableResource = when {
        LocalInDarkTheme.current -> Res.drawable.hard_neon_dark
        else -> Res.drawable.hard_neon
    }

    Image(
        painter = painterResource(drawableResource),
        contentDescription = stringResource(Res.string.app_name),
        modifier = modifier,
    )
}

//@Composable
//fun Wordmark(modifier: Modifier = Modifier) {
//    Text(
//        text = buildAnnotatedString {
//            val appName = stringResource(Res.string.app_name_lowercase)
//            withStyle(
//                style = SpanStyle(color = MaterialTheme.colorScheme.tertiary),
//            ) {
//                append(appName.first())
//            }
//            withStyle(
//                style = SpanStyle(color = MaterialTheme.colorScheme.primary),
//            ) {
//                append(appName.substring(1))
//            }
//        },
//        style = MaterialTheme.typography.displayLarge,
//        fontSize = 128.sp,
//        lineHeight = 20.sp,
//        modifier = modifier,
//    )
//}