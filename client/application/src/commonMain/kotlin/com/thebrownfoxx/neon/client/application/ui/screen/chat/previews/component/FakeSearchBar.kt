package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.component.AppBar
import com.thebrownfoxx.neon.client.application.ui.component.Spacer
import com.thebrownfoxx.neon.client.application.ui.component.TopBarScrim
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.search_conversations
import org.jetbrains.compose.resources.stringResource

@Composable
fun FakeSearchBar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    TopBarScrim(
        modifier = modifier,
    ) {
        AppBar(modifier = modifier.padding(contentPadding)) {
            Row(
                modifier = Modifier.height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Spacer(width = 16.dp)
                    SearchIcon()
                    Spacer(width = 16.dp)
                    SearchLabel()
                    Spacer(width = 16.dp)
                }
            }
        }
    }
}

@Composable
private fun SearchIcon() {
    Icon(
        imageVector = Icons.TwoTone.Search,
        contentDescription = null,
    )
}

@Composable
private fun SearchLabel() {
    Text(
        text = stringResource(Res.string.search_conversations),
        style = MaterialTheme.typography.bodyLarge,
    )
}
