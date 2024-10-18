package com.thebrownfoxx.neon.client.application.ui.screen.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Login
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.thebrownfoxx.neon.client.application.ui.component.common.ButtonIconText
import com.thebrownfoxx.neon.client.application.ui.component.common.ThemedButton
import com.thebrownfoxx.neon.client.application.ui.component.common.ThemedButtonDefaults
import com.thebrownfoxx.neon.client.application.ui.component.common.textfield.PasswordField
import com.thebrownfoxx.neon.client.application.ui.component.common.textfield.ThemedFilledTextFieldContainer
import com.thebrownfoxx.neon.client.application.ui.component.common.textfield.ThemedTextField
import com.thebrownfoxx.neon.client.application.ui.extension.NavigationBarHeight
import com.thebrownfoxx.neon.client.application.ui.extension.PaddingSide
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.client.application.ui.extension.Side
import com.thebrownfoxx.neon.client.application.ui.extension.bottomPadding
import com.thebrownfoxx.neon.client.application.ui.extension.paddingExcept
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.app_name_lowercase
import neon.client.application.generated.resources.join
import neon.client.application.generated.resources.login
import neon.client.application.generated.resources.password
import neon.client.application.generated.resources.username
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// TODO: If the user insists on logging in with missing credentials, reemphasize the warning.
// TODO: If there is an error, open the relevant text field
@Composable
fun LoginScreen(
    state: LoginScreenState,
    eventHandler: LoginScreenEventHandler,
    modifier: Modifier = Modifier,
) {
    val bottomPadding = max(NavigationBarHeight, 16.dp)

    with(eventHandler) {
        with(state) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = modifier.imePadding(),
            ) {
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState(), reverseScrolling = true)
                        .statusBarsPadding()
                        .padding(16.dp.paddingExcept(PaddingSide.Bottom))
                        .padding(bottom = bottomPadding)
                        .consumeWindowInsets(WindowInsets.navigationBars),
                ) {
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
                    )
                    ThemedFilledTextFieldContainer(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = loginState != LoginState.LoggingIn,
                        shape = RoundedCorners(Side.Top).toShape(),
                    ) {
                        ThemedTextField(
                            label = stringResource(Res.string.username),
                            value = username,
                            onValueChange = onUsernameChange,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Password,
                                autoCorrect = false,
                            ),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemedFilledTextFieldContainer(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = loginState != LoginState.LoggingIn,
                        shape = RoundedCorners(Side.Bottom).toShape(),
                    ) {
                        PasswordField(
                            label = stringResource(Res.string.password),
                            value = password,
                            onValueChange = onPasswordChange,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Password,
                                autoCorrect = false,
                            ),
                            keyboardActions = KeyboardActions { onLoginClick() },
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LoginStateIndicator(
                        state = loginState,
                        modifier = Modifier.fillMaxWidth(),
                        padding = 12.dp.bottomPadding,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemedButton(
                            onClick = onJoinClick,
                            roundedCorners = RoundedCorners(Side.Start),
                            enabled = loginState != LoginState.LoggingIn,
                            colors = ThemedButtonDefaults.weakColors,
                        ) {
                            Text(text = stringResource(Res.string.join))
                        }
                        ThemedButton(
                            onClick = onLoginClick,
                            roundedCorners = RoundedCorners(Side.End),
                            modifier = Modifier.weight(1f),
                            enabled = loginState != LoginState.LoggingIn,
                        ) {
                            ButtonIconText(
                                icon = Icons.AutoMirrored.TwoTone.Login,
                                iconContentDescription = stringResource(Res.string.login),
                                text = stringResource(Res.string.login),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        LoginScreen(
            state = LoginScreenState(),
            eventHandler = LoginScreenEventHandler.Blank,
        )
    }
}