package com.thebrownfoxx.neon.client.application.ui.screen.chat.previews.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thebrownfoxx.neon.client.application.ui.component.AppBarDefaults
import com.thebrownfoxx.neon.client.application.ui.component.TopBarScrim
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.search_conversations
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FakeSearchBar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    TopBarScrim(modifier = modifier) {
        SearchBar(
            query = "",
            onQueryChange = {},
            onSearch = {},
            active = false,
            onActiveChange = {},
            leadingIcon = {
                Icon(imageVector = Icons.TwoTone.Search, contentDescription = null)
            },
            placeholder = { Text(text = stringResource(Res.string.search_conversations)) },
            shape = CircleShape,
            colors = SearchBarDefaults.colors(
                containerColor = AppBarDefaults.ContainerColor.copy(alpha = 0.9f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
        ) {}
    }
}