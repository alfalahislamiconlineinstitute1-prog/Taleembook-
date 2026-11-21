package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AlFalahExtendedColors(
    val bg: Color,
    val card: Color,
    val fieldBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val line: Color,
    val goldTint: Color,
    val goldTintText: Color,
    val dangerTint: Color,
    val dangerText: Color,
    val successTint: Color,
    val successText: Color,
    val purpleTint: Color,
    val purpleTintText: Color,
    val purpleHeader: Color,
    val purpleMid: Color,
    val purpleAccent: Color,
    val goldPrimary: Color,
    val goldBright: Color,
    val cardBorder: Color,
    val isDark: Boolean
)

private val LightExtendedColors = AlFalahExtendedColors(
    bg = WarmCream,
    card = PureWhite,
    fieldBg = WarmCream,
    textPrimary = Charcoal900,
    textSecondary = Charcoal700,
    textMuted = Charcoal400,
    line = LineLight,
    goldTint = Gold100,
    goldTintText = GoldTintTextLight,
    dangerTint = DangerTintLight,
    dangerText = DangerRed,
    successTint = SuccessTintLight,
    successText = SuccessGreen,
    purpleTint = PurpleTintLight,
    purpleTintText = Purple600,
    purpleHeader = Purple900,
    purpleMid = Purple700,
    purpleAccent = Purple600,
    goldPrimary = Gold500,
    goldBright = Gold400,
    cardBorder = Color(0x0A2A0E4A),
    isDark = false
)

private val DarkExtendedColors = AlFalahExtendedColors(
    bg = DarkBg,
    card = DarkCard,
    fieldBg = DarkCream,
    textPrimary = DarkText,
    textSecondary = DarkCharcoal700,
    textMuted = DarkCharcoal400,
    line = DarkLine,
    goldTint = DarkGoldTint,
    goldTintText = Gold400,
    dangerTint = DarkDangerTint,
    dangerText = DarkDangerText,
    successTint = DarkSuccessTint,
    successText = SuccessGreen,
    purpleTint = DarkPurpleTint,
    purpleTintText = DarkCharcoal700,
    purpleHeader = Purple900,
    purpleMid = Purple700,
    purpleAccent = Purple500,
    goldPrimary = Gold500,
    goldBright = Gold400,
    cardBorder = DarkLine,
    isDark = true
)

val LocalAlFalahColors = staticCompositionLocalOf { LightExtendedColors }

private val AlFalahLightColorScheme = lightColorScheme(
    primary = Purple900,
    onPrimary = PureWhite,
    primaryContainer = PurpleTintLight,
    onPrimaryContainer = Purple900,
    secondary = Gold500,
    onSecondary = Purple900,
    secondaryContainer = Gold100,
    onSecondaryContainer = GoldTintTextLight,
    tertiary = Purple600,
    onTertiary = PureWhite,
    background = WarmCream,
    onBackground = Charcoal900,
    surface = PureWhite,
    onSurface = Charcoal900,
    surfaceVariant = WarmCream,
    onSurfaceVariant = Charcoal700,
    outline = LineLight,
    error = DangerRed,
    onError = PureWhite
)

private val AlFalahDarkColorScheme = darkColorScheme(
    primary = Gold400,
    onPrimary = Purple900,
    primaryContainer = Purple700,
    onPrimaryContainer = DarkText,
    secondary = Gold500,
    onSecondary = Purple900,
    secondaryContainer = DarkGoldTint,
    onSecondaryContainer = Gold400,
    tertiary = Purple500,
    onTertiary = PureWhite,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkCard,
    onSurface = DarkText,
    surfaceVariant = DarkCream,
    onSurfaceVariant = DarkCharcoal700,
    outline = DarkLine,
    error = DarkDangerText,
    onError = Purple900
)

object AlFalahTheme {
    val colors: AlFalahExtendedColors
        @Composable get() = LocalAlFalahColors.current
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AlFalahDarkColorScheme else AlFalahLightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(LocalAlFalahColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
