package com.thebrownfoxx.neon.client.application.ui.screen.login.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Login
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.Spacer
import com.thebrownfoxx.neon.client.application.ui.component.common.ButtonIconText
import com.thebrownfoxx.neon.client.application.ui.component.common.ThemedButton
import com.thebrownfoxx.neon.client.application.ui.component.common.ThemedButtonDefaults
import com.thebrownfoxx.neon.client.application.ui.component.common.textfield.PasswordField
import com.thebrownfoxx.neon.client.application.ui.component.common.textfield.ThemedFilledTextFieldContainer
import com.thebrownfoxx.neon.client.application.ui.component.common.textfield.ThemedTextField
import com.thebrownfoxx.neon.client.application.ui.extension.RoundedCorners
import com.thebrownfoxx.neon.client.application.ui.extension.Side
import com.thebrownfoxx.neon.client.application.ui.extension.bottomPadding
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenState
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginState
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import neon.client.application.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoginScreenContent(
    state: LoginScreenState,
    eventHandler: LoginScreenEventHandler,
    modifier: Modifier = Modifier,
) {
    with(state) {
        with(eventHandler) {
            Column(modifier = modifier) {
                Wordmark()
                UsernameField(
                    username = username,
                    onUsernameChange = onUsernameChange,
                    loginState = loginState,
                )
                Spacer(modifier = Modifier.height(8.dp))
                PasswordField(
                    password = password,
                    onPasswordChange = onPasswordChange,
                    onLogin = onLogin,
                    loginState = loginState,
                )
                Spacer(modifier = Modifier.height(16.dp))
                LoginStateIndicator(
                    state = loginState,
                    modifier = Modifier.fillMaxWidth(),
                    padding = 12.dp.bottomPadding,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    JoinButton(onJoin = onJoin, loginState = loginState)
                    LoginButton(
                        onLogin = onLogin,
                        loginState = loginState,
                    )
                }
            }
        }
    }
}

@Composable
private fun UsernameField(
    username: String,
    onUsernameChange: (String) -> Unit,
    loginState: LoginState,
) {
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
                autoCorrectEnabled = false,
            ),
        )
    }
}

@Composable
private fun PasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    loginState: LoginState,
) {
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
                autoCorrectEnabled = false,
            ),
            keyboardActions = KeyboardActions { onLogin() },
        )
    }
}

@Composable
private fun JoinButton(
    onJoin: () -> Unit,
    loginState: LoginState,
) {
    ThemedButton(
        onClick = onJoin,
        roundedCorners = RoundedCorners(Side.Start),
        enabled = loginState != LoginState.LoggingIn,
        colors = ThemedButtonDefaults.weakColors,
    ) {
        Text(text = stringResource(Res.string.join))
    }
}

@Composable
private fun RowScope.LoginButton(
    onLogin: () -> Unit,
    loginState: LoginState,
) {
    ThemedButton(
        onClick = onLogin,
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

@Preview
@Composable
private fun Preview() {
    NeonTheme {
        LoginScreenContent(
            state = LoginScreenState(),
            eventHandler = LoginScreenEventHandler.Blank,
            modifier = Modifier.padding(16.dp),
        )
    }
}