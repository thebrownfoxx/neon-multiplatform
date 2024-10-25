package com.thebrownfoxx.neon.client.application.ui.component.avatar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thebrownfoxx.neon.client.application.ui.theme.NeonTheme

@Preview
@Composable
private fun NullPreview() {
    NeonTheme {
        Surface {
            LargePlaceholderAvatar(
                placeholder = null,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun DarkPreview() {
    NeonTheme(darkTheme = true) {
        Surface {
            LargePlaceholderAvatar(
                placeholder = "landooo",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun LargePreview() {
    NeonTheme {
        Surface {
            LargePlaceholderAvatar(
                placeholder = "landooo",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun MediumPreview() {
    NeonTheme {
        Surface {
            MediumPlaceholderAvatar(
                placeholder = "landooo",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview
@Composable
private fun SmallPreview() {
    NeonTheme {
        Surface {
            SmallPlaceholderAvatar(
                placeholder = "landooo",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}