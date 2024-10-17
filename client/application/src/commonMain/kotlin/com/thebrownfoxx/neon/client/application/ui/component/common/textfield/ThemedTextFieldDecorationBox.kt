package com.thebrownfoxx.neon.client.application.ui.component.common.textfield

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.extension.horizontal
import com.thebrownfoxx.neon.client.application.ui.extension.onlyIf
import com.thebrownfoxx.neon.client.application.ui.extension.padding
import com.thebrownfoxx.neon.client.application.ui.extension.vertical

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
fun DecorationBox(
    value: String,
    enabled: Boolean,
    placeholder: String?,
    label: String?,
    isError: Boolean,
    singleLine: Boolean,
    visualTransformation: VisualTransformation,
    interactionSource: MutableInteractionSource,
    contentColor: Color,
    bringIntoViewRequester: BringIntoViewRequester,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    innerTextField: @Composable () -> Unit,
) {
    TextFieldDefaults.DecorationBox(
        value = value,
        innerTextField = {
            InnerTextFieldBox(
                bringIntoViewRequester = bringIntoViewRequester,
                singleLine = singleLine,
                hasLabel = label != null,
                showingPlaceholder = placeholder != null && value.isEmpty(),
                contentPadding = contentPadding,
                content = innerTextField,
            )
        },
        enabled = enabled,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        isError = isError,
        container = {},
        contentPadding = 0.dp.padding,
        label = label?.let {
            {
                Label(
                    label = it,
                    contentColor = contentColor,
                    contentPadding = contentPadding.horizontal,
                )
            }
        },
        placeholder = placeholder?.let {
            {
                Placeholder(
                    placeholder = it,
                    contentColor = contentColor,
                    contentPadding = contentPadding.horizontal,
                )
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InnerTextFieldBox(
    bringIntoViewRequester: BringIntoViewRequester,
    singleLine: Boolean,
    hasLabel: Boolean,
    showingPlaceholder: Boolean,
    contentPadding: PaddingValues,
    content: @Composable () -> Unit,
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val animationSpec = spring<IntSize>(stiffness = Spring.StiffnessHigh)

    Box(
        modifier = Modifier
            .onlyIf(singleLine) { horizontalScroll(horizontalScrollState, reverseScrolling = true) }
            .padding(contentPadding.horizontal)
            .bringIntoViewRequester(bringIntoViewRequester)
            .onlyIf(!singleLine) {
                verticalScroll(verticalScrollState, reverseScrolling = true)
            }
            .onlyIf(!hasLabel && !showingPlaceholder) { padding(contentPadding.vertical) }
            .onlyIf(!singleLine) {
                animateContentSize(animationSpec)
            },
    ) {
        content()
    }
}

@Composable
private fun Label(
    label: String,
    contentColor: Color,
    contentPadding: PaddingValues,
) {
    Text(
        text = label,
        modifier = Modifier.padding(contentPadding),
        color = contentColor,
    )
}

@Composable
private fun Placeholder(
    placeholder: String,
    contentColor: Color,
    contentPadding: PaddingValues,
) {
    Label(
        label = placeholder,
        contentColor = contentColor.copy(alpha = 0.7f),
        contentPadding = contentPadding,
    )
}
