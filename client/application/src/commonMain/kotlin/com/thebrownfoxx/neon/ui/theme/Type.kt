package com.thebrownfoxx.neon.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import neon.client.application.generated.resources.Res
import neon.client.application.generated.resources.inter_black
import neon.client.application.generated.resources.inter_bold
import neon.client.application.generated.resources.inter_extra_bold
import neon.client.application.generated.resources.inter_extra_light
import neon.client.application.generated.resources.inter_light
import neon.client.application.generated.resources.inter_medium
import neon.client.application.generated.resources.inter_regular
import neon.client.application.generated.resources.inter_semi_bold
import neon.client.application.generated.resources.inter_thin
import neon.client.application.generated.resources.inter_tight_black
import neon.client.application.generated.resources.inter_tight_bold
import neon.client.application.generated.resources.inter_tight_extra_bold
import neon.client.application.generated.resources.inter_tight_extra_light
import neon.client.application.generated.resources.inter_tight_light
import neon.client.application.generated.resources.inter_tight_medium
import neon.client.application.generated.resources.inter_tight_regular
import neon.client.application.generated.resources.inter_tight_semi_bold
import neon.client.application.generated.resources.inter_tight_thin
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.ui.tooling.preview.Preview

//val provider = GoogleFont.Provider(
//    providerAuthority = "com.google.android.gms.fonts",
//    providerPackage = "com.google.android.gms",
//    certificates = Res.array.com_google_android_gms_fonts_certs
//)
//
//val bodyFontFamily = FontFamily(
//    Font(
//        googleFont = GoogleFont("Inter"),
//        fontProvider = provider,
//    )
//)
//
//val displayFontFamily = FontFamily(
//    Font(
//        googleFont = GoogleFont("Inter Tight"),
//        fontProvider = provider,
//    )
//)

val bodyFontFamily @Composable get() = FontFamily(
    Font(
        Res.font.inter_black,
        weight = FontWeight.Black,
    ),
    Font(
        Res.font.inter_extra_bold,
        weight = FontWeight.ExtraBold,
    ),
    Font(
        Res.font.inter_bold,
        weight = FontWeight.Bold,
    ),
    Font(
        Res.font.inter_semi_bold,
        weight = FontWeight.SemiBold,
    ),
    Font(
        Res.font.inter_medium,
        weight = FontWeight.Medium,
    ),
    Font(
        Res.font.inter_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        Res.font.inter_light,
        weight = FontWeight.Light,
    ),
    Font(
        Res.font.inter_extra_light,
        weight = FontWeight.ExtraLight,
    ),
    Font(
        Res.font.inter_thin,
        weight = FontWeight.Thin,
    ),
)

val displayFontFamily @Composable get() = FontFamily(
    Font(
        Res.font.inter_tight_black,
        weight = FontWeight.Black,
    ),
    Font(
        Res.font.inter_tight_extra_bold,
        weight = FontWeight.ExtraBold,
    ),
    Font(
        Res.font.inter_tight_bold,
        weight = FontWeight.Bold,
    ),
    Font(
        Res.font.inter_tight_semi_bold,
        weight = FontWeight.SemiBold,
    ),
    Font(
        Res.font.inter_tight_medium,
        weight = FontWeight.Medium,
    ),
    Font(
        Res.font.inter_tight_regular,
        weight = FontWeight.Normal,
    ),
    Font(
        Res.font.inter_tight_light,
        weight = FontWeight.Light,
    ),
    Font(
        Res.font.inter_tight_extra_light,
        weight = FontWeight.ExtraLight,
    ),
    Font(
        Res.font.inter_tight_thin,
        weight = FontWeight.Thin,
    ),
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography @Composable get() = Typography(
    displayLarge = baseline.displayLarge.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Black,
    ),
    displayMedium = baseline.displayMedium.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Black,
    ),
    displaySmall = baseline.displaySmall.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Black,
    ),
    headlineLarge = baseline.headlineLarge.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    headlineMedium = baseline.headlineMedium.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    headlineSmall = baseline.headlineSmall.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Bold,
    ),
    titleLarge = baseline.titleLarge.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Medium,
    ),
    titleMedium = baseline.titleMedium.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Medium,
    ),
    titleSmall = baseline.titleSmall.copy(
        fontFamily = displayFontFamily,
        fontWeight = FontWeight.Medium,
    ),
    bodyLarge = baseline.bodyLarge.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
    ),
    bodyMedium = baseline.bodyMedium.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
    ),
    bodySmall = baseline.bodySmall.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Normal,
    ),
    labelLarge = baseline.labelLarge.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
    ),
    labelMedium = baseline.labelMedium.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
    ),
    labelSmall = baseline.labelSmall.copy(
        fontFamily = bodyFontFamily,
        fontWeight = FontWeight.Medium,
    ),
)

@Preview
@Composable
private fun TypographyPreview() {
    MaterialTheme(typography = AppTypography) {
        Column {
            val styles = listOf(
                "Display Large" to MaterialTheme.typography.displayLarge,
                "Display Medium" to MaterialTheme.typography.displayMedium,
                "Display Small" to MaterialTheme.typography.displaySmall,
                "Headline Large" to MaterialTheme.typography.headlineLarge,
                "Headline Medium" to MaterialTheme.typography.headlineMedium,
                "Headline Small" to MaterialTheme.typography.headlineSmall,
                "Title Large" to MaterialTheme.typography.titleLarge,
                "Title Medium" to MaterialTheme.typography.titleMedium,
                "Title Small" to MaterialTheme.typography.titleSmall,
                "Body Large" to MaterialTheme.typography.bodyLarge,
                "Body Medium" to MaterialTheme.typography.bodyMedium,
                "Body Small" to MaterialTheme.typography.bodySmall,
                "Label Large" to MaterialTheme.typography.labelLarge,
                "Label Medium" to MaterialTheme.typography.labelMedium,
                "Label Small" to MaterialTheme.typography.labelSmall,
            )
            for ((name, style) in styles) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = "Lorem ipsum",
                    style = style,
                )
                HorizontalDivider()
            }
        }
    }
}