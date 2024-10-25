package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.scrim.GradientDirection
import com.thebrownfoxx.neon.client.application.ui.component.scrim.GradientScrimBox
import com.thebrownfoxx.neon.client.application.ui.extension.end
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.extension.plus
import com.thebrownfoxx.neon.client.application.ui.extension.start
import com.thebrownfoxx.neon.client.application.ui.extension.vertical
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// TODO: Add vertical scrims
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = LocalThemedTextFieldEnabled.current,
    readOnly: Boolean = false,
    placeholder: String? = null,
    label: String? = null,
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
    iconAlignment: Alignment = Alignment.Center,
    isError: Boolean = LocalThemedTextFieldIsError.current,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: ThemedTextFieldStateColors = LocalThemedTextFieldColors.currentOrDefault,
) {
    val focusRequester = FocusRequester()
    val keyboardController = LocalSoftwareKeyboardController.current

    var focused by remember { mutableStateOf(false) }

    val stateColors = colors.getStateColors(
        focused = focused,
        enabled = enabled,
        isError = isError,
    )

    val accentColor by animateColorAsState(
        targetValue = stateColors.accentColor,
        label = "accentColor",
    )

    val contentColor by animateColorAsState(
        targetValue = stateColors.contentColor,
        label = "contentColor",
    )

    val leadingIconWithScrim = @Composable {
        IconScrim(
            gradientDirection = GradientDirection.StartEnd,
            contentAlignment = iconAlignment,
            content = leadingIcon,
        )
    }

    val trailingIconWithScrim = @Composable {
        IconScrim(
            gradientDirection = GradientDirection.EndStart,
            contentAlignment = iconAlignment,
            content = trailingIcon,
        )
    }

    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    val textFieldValue = textFieldValueState.copy(text = value)

    SideEffect {
        if (textFieldValue.selection != textFieldValueState.selection ||
            textFieldValue.composition != textFieldValueState.composition
        ) {
            textFieldValueState = textFieldValue
        }
    }
    var lastTextValue by remember(value) { mutableStateOf(value) }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .onFocusChanged { focused = it.hasFocus }
            .defaultMinSize(
                minWidth = TextFieldDefaults.MinWidth,
                minHeight = TextFieldDefaults.MinHeight,
            )
            .focusProperties { canFocus = false }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                focusRequester.requestFocus()
                keyboardController?.show()
            },
    ) {
        CompositionLocalProvider(
            LocalThemedTextFieldFocused provides focused,
            LocalThemedTextFieldEnabled provides enabled,
            LocalThemedTextFieldIsError provides isError,
            LocalThemedTextFieldColors provides colors,
            LocalContentColor provides contentColor,
        ) {
            ThemedTextFieldLayout(
                leadingIcon = leadingIconWithScrim,
                trailingIcon = trailingIconWithScrim,
            ) { innerPadding ->
                val density = LocalDensity.current
                val startPadding = innerPadding.start
                val endPadding = innerPadding.end

                LaunchedEffect(textFieldValue, textLayoutResult) {
                    val cursorRect = textLayoutResult?.getCursorRect(textFieldValue.selection.start)
                    val paddedCursorRect = with(density) {
                        cursorRect?.copy(
                            left = cursorRect.left - startPadding.toPx(),
                            right = cursorRect.right + endPadding.toPx(),
                        )
                    }
                    bringIntoViewRequester.bringIntoView(paddedCursorRect)
                }

                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { newTextFieldValueState ->
                        textFieldValueState = newTextFieldValueState

                        val stringChangedSinceLastInvocation =
                            lastTextValue != newTextFieldValueState.text
                        lastTextValue = newTextFieldValueState.text

                        if (stringChangedSinceLastInvocation) {
                            onValueChange(newTextFieldValueState.text)
                        }
                    },
                    enabled = enabled,
                    readOnly = readOnly,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = singleLine,
                    maxLines = maxLines,
                    minLines = minLines,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    cursorBrush = SolidColor(accentColor),
                    onTextLayout = { textLayoutResult = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                ) { innerTextField ->
                    DecorationBox(
                        value = textFieldValue.text,
                        enabled = enabled,
                        placeholder = placeholder,
                        label = label,
                        isError = isError,
                        singleLine = singleLine,
                        visualTransformation = visualTransformation,
                        interactionSource = interactionSource,
                        contentColor = contentColor,
                        bringIntoViewRequester = bringIntoViewRequester,
                        innerTextField = innerTextField,
                        contentPadding = innerPadding + 12.dp.padding.vertical,
                    )
                }
            }
        }
    }
}

@Composable
private fun IconScrim(
    gradientDirection: GradientDirection,
    contentAlignment: Alignment,
    content: @Composable () -> Unit,
) {
    val scrimColor by animateColorAsState(
        targetValue = LocalThemedTextFieldColors.currentOrDefault
            .getStateColors(
                focused = LocalThemedTextFieldFocused.current,
                enabled = LocalThemedTextFieldEnabled.current,
                isError = LocalThemedTextFieldIsError.current,
            ).containerColor,
        label = "scrimColor",
    )

    GradientScrimBox(
        gradientDirection = gradientDirection,
        scrimColor = scrimColor,
        maxAlpha = 0.9f,
        thresholdAlpha = 0.9f,
        threshold = 0.5f,
    ) {
        Box(modifier = Modifier.align(contentAlignment)) {
            content()
        }
    }
}

@Preview
@Composable
private fun InteractivePreview() {
    val focusManager = LocalFocusManager.current

    NeonTheme {
        Box(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { focusManager.clearFocus() }
        ) {
            var value by remember { mutableStateOf("") }

            ThemedTextField(
                label = "Label",
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun PlainEmptyPreview() {
    NeonTheme {
        ThemedTextField(
            value = "",
            onValueChange = {},
        )
    }
}

@Preview
@Composable
private fun LabeledPreview() {
    NeonTheme {
        ThemedTextField(
            label = "Label",
            value = "",
            onValueChange = {},
        )
    }
}

@Preview
@Composable
private fun LabeledWithContentPreview() {
    NeonTheme {
        ThemedTextField(
            label = "Label",
            value = "Content",
            onValueChange = {},
        )
    }
}

@Preview
@Composable
private fun WithPlaceholderPreview() {
    NeonTheme {
        ThemedTextField(
            placeholder = "Placeholder",
            value = "",
            onValueChange = {},
        )
    }
}

@Preview
@Composable
private fun WithLeadingIconPreview() {
    NeonTheme {
        ThemedTextField(
            label = "Username",
            leadingIcon = {
                ThemedTextFieldLeadingIcon {
                    Icon(imageVector = Icons.TwoTone.Person, contentDescription = null)
                }
            },
            value = "",
            onValueChange = {},
        )
    }
}

@Preview
@Composable
private fun WithTrailingIconPreview() {
    NeonTheme {
        ThemedTextField(
            label = "Password",
            trailingIcon = {
                ThemedTextFieldTrailingIcon {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.TwoTone.VisibilityOff, contentDescription = null)
                    }
                }
            },
            value = "u3egfuyuygfuyegfyuegfgegfegfgeuigfeygfigyegfegfierigferufdfhuwiehfeuf",
            onValueChange = {},
        )
    }
}