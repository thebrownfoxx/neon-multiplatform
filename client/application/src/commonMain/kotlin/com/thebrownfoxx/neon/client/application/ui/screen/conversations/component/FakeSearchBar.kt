package com.thebrownfoxx.neon.client.application.ui.screen.conversations.component

//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.twotone.Search
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.SearchBar
//import androidx.compose.material3.SearchBarDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.unit.dp
//import com.thebrownfoxx.neon.android.R
//import com.thebrownfoxx.neon.android.ui.component.AppBarDefaults
//import com.thebrownfoxx.neon.android.ui.component.TopBarScrim
//import com.thebrownfoxx.neon.android.ui.extension.PaddingSide
//import com.thebrownfoxx.neon.android.ui.extension.StatusBarPadding
//import com.thebrownfoxx.neon.android.ui.extension.paddingExcept
//import com.thebrownfoxx.neon.android.ui.extension.plus
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun FakeSearchBar(modifier: Modifier = Modifier) {
//    TopBarScrim(
//        modifier = modifier,
//        contentPadding = 16.dp.paddingExcept(PaddingSide.Top) +
//                StatusBarPadding,
//    ) {
//        SearchBar(
//            query = "",
//            onQueryChange = {},
//            onSearch = {},
//            active = false,
//            onActiveChange = {},
//            leadingIcon = {
//                Icon(imageVector = Icons.TwoTone.Search, contentDescription = null)
//            },
//            placeholder = { Text(text = stringResource(R.string.search_conversations)) },
//            shape = MaterialTheme.shapes.medium,
//            colors = SearchBarDefaults.colors(
//                containerColor = AppBarDefaults.ContainerColor.copy(alpha = 0.9f),
//            ),
//            modifier = Modifier.fillMaxWidth(),
//        ) {}
//    }
//}