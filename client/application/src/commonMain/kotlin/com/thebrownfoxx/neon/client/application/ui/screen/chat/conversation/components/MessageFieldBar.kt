package com.thebrownfoxx.neon.client.application.ui.screen.chat.conversation.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.BottomBarScrim
import com.thebrownfoxx.neon.client.application.ui.component.common.textfield.AppBarColors
import com.thebrownfoxx.neon.client.application.ui.component.common.textfield.FilledTextField
import com.thebrownfoxx.neon.client.application.ui.component.common.textfield.TextFieldIcon
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.message
import neon.client.application.generated.resources.send
import org.jetbrains.compose.resources.stringResource

@Composable
fun MessageFieldBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    BottomBarScrim(modifier = modifier) {
        FilledTextField(
            value = message,
            onValueChange = onMessageChange,
            placeholder = stringResource(Res.string.message),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.AppBarColors,
            trailingIcon = { SendButton(onSend) },
            iconAlignment = Alignment.BottomCenter,
            singleLine = false,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxWidth()
                .sizeIn(maxHeight = 128.dp),
        )
    }
}

@Composable
private fun SendButton(onSend: () -> Unit) {
    TextFieldIcon {
        IconButton(onClick = onSend) {
            Icon(
                imageVector = Icons.AutoMirrored.TwoTone.Send,
                contentDescription = stringResource(Res.string.send),
            )
        }
    }
}