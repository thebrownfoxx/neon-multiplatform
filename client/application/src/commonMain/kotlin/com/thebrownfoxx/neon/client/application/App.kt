package com.thebrownfoxx.neon.client.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thebrownfoxx.neon.client.application.ui.extension.LocalWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.extension.calculateWindowSizeClass
import com.thebrownfoxx.neon.client.application.ui.screen.login.LoginViewModel
import com.thebrownfoxx.neon.client.application.ui.screen.login.state.LoginScreenEventHandler
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thebrownfoxx.neon.client.application.http.HttpClient
import com.thebrownfoxx.neon.client.application.ui.component.avatar.state.SingleAvatarState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.component.ConversationPreview
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.ConversationPreviewState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.PreviewContentState
import com.thebrownfoxx.neon.client.application.ui.screen.conversations.state.SentBySelfState
import com.thebrownfoxx.neon.client.application.ui.component.delivery.state.DeliveryState
import com.thebrownfoxx.neon.client.service.default.DefaultAuthenticator
import com.thebrownfoxx.neon.client.service.default.InMemoryTokenStorage
import com.thebrownfoxx.neon.common.extension.toLocalDateTime
import kotlinx.datetime.Clock

@Composable
@Preview
fun App() {
    CompositionLocalProvider(LocalWindowSizeClass provides calculateWindowSizeClass()) {
        NeonTheme {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                val loginViewModel = viewModel {
                    val authenticator = DefaultAuthenticator(
                        HttpClient(),
                        InMemoryTokenStorage(),
                    )
                    LoginViewModel(authenticator)
                }
                val loginScreenState by loginViewModel.state.collectAsStateWithLifecycle()
                val loginScreenEventHandler = with(loginViewModel) {
                    LoginScreenEventHandler(
                        onUsernameChange = ::onUsernameChange,
                        onPasswordChange = ::onPasswordChange,
                        onLogin = ::onLogin,
                        onJoin = ::onJoin,
                    )
                }

//                LoginScreen(
//                    state = loginScreenState,
//                    eventHandler = loginScreenEventHandler,
//                )
                ConversationPreview(
                    conversationPreview = ConversationPreviewState(
                        avatar = SingleAvatarState(url = null, placeholder = "John"),
                        name = "John",
                        content = PreviewContentState(
                            message = "Hello",
                            timestamp = Clock.System.now().toLocalDateTime(),
                            delivery = DeliveryState.Delivered,
                            senderState = SentBySelfState,
                        )
                    ),
                    read = true,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {},
                )
            }
        }
    }
}