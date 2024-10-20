package com.thebrownfoxx.neon.client.application.ui.screen.login.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CloudOff
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.InformationCard
import com.thebrownfoxx.neon.client.application.ui.component.InformationCardIconText
import com.thebrownfoxx.neon.client.application.ui.component.InformationCardProgressIndicator
import com.thebrownfoxx.neon.client.application.ui.component.common.ExpandAxis
import com.thebrownfoxx.neon.client.application.ui.component.common.ThemedAnimatedVisibility
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.extension.rememberCache
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState.*
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.connection_error
import neon.client.application.generated.resources.connection_error_message
import neon.client.application.generated.resources.credentials_incorrect_message
import neon.client.application.generated.resources.credentials_missing_message
import neon.client.application.generated.resources.error
import neon.client.application.generated.resources.logging_in_message
import neon.client.application.generated.resources.password_missing_message
import neon.client.application.generated.resources.username_missing_message
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoginStateIndicator(
    state: LoginState,
    modifier: Modifier = Modifier,
    padding: PaddingValues = 0.dp.padding,
) {
    val cachedStatus = rememberCache(target = state) { state != Idle }

    ThemedAnimatedVisibility(
        visible = state != Idle,
        modifier = modifier,
        expandAxis = ExpandAxis.Vertical,
    ) {
        if (cachedStatus != Idle && cachedStatus != null) {
            val containerColor by animateColorAsState(
                targetValue = when (state) {
                    LoggingIn, is CredentialsMissing ->
                        MaterialTheme.colorScheme.surfaceContainer

                    else -> MaterialTheme.colorScheme.errorContainer
                },
                label = "containerColor",
            )

            val contentColor by animateColorAsState(
                targetValue = when (state) {
                    LoggingIn -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onErrorContainer
                },
                label = "contentColor",
            )

            InformationCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                progressIndicator = {
                    InformationCardProgressIndicator(visible = state == LoggingIn) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                },
                containerColor = containerColor,
                contentColor = contentColor,
            ) {
                AnimatedContent(
                    targetState = cachedStatus,
                    label = "statusContent",
                ) { targetStatus ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        when (targetStatus) {
                            Idle -> error("This should never happen")
                            is CredentialsMissing -> {
                                val text = when {
                                    targetStatus.usernameMissing && targetStatus.passwordMissing ->
                                        stringResource(Res.string.credentials_missing_message)

                                    targetStatus.usernameMissing ->
                                        stringResource(Res.string.username_missing_message)

                                    targetStatus.passwordMissing ->
                                        stringResource(Res.string.password_missing_message)

                                    else ->
                                        throw IllegalStateException(
                                            "CredentialsMissing should have at least one missing " +
                                                    "credential",
                                        )
                                }
                                Text(text = text)
                            }

                            LoggingIn -> {
                                Text(text = stringResource(Res.string.logging_in_message))
                            }

                            CredentialsIncorrect -> {
                                InformationCardIconText(
                                    icon = Icons.TwoTone.Warning,
                                    iconContentDescription = stringResource(Res.string.error),
                                    text = stringResource(Res.string.credentials_incorrect_message),
                                )
                            }

                            ConnectionError -> {
                                InformationCardIconText(
                                    icon = Icons.TwoTone.CloudOff,
                                    iconContentDescription = stringResource(Res.string.connection_error),
                                    text = stringResource(Res.string.connection_error_message)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun IdleLoginPreview() {
    NeonTheme {
        LoginStateIndicator(
            state = Idle,
            padding = 16.dp.padding,
        )
    }
}

@Preview
@Composable
private fun CredentialsMissingPreview() {
    NeonTheme {
        LoginStateIndicator(
            state = CredentialsMissing(
                usernameMissing = true,
                passwordMissing = true,
            ),
            padding = 16.dp.padding,
        )
    }
}

@Preview
@Composable
private fun LoggingInPreview() {
    NeonTheme {
        LoginStateIndicator(
            state = LoggingIn,
            padding = 16.dp.padding,
        )
    }
}

@Preview
@Composable
private fun CredentialsIncorrectPreview() {
    NeonTheme {
        LoginStateIndicator(
            state = CredentialsIncorrect,
            padding = 16.dp.padding,
        )
    }
}

@Preview
@Composable
private fun ConnectionErrorPreview() {
    NeonTheme {
        LoginStateIndicator(
            state = ConnectionError,
            padding = 16.dp.padding,
        )
    }
}