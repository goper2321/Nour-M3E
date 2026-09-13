package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// =========================================================================
// Material 3 Expressive Monochrome Palette
// Distinct tonal stops from pure pitch black to crystalline white
// =========================================================================

val MonoPitchBlack = Color(0xFF000000)
val MonoObsidian = Color(0xFF09090B)
val MonoCharcoal = Color(0xFF141416)
val MonoGraphite = Color(0xFF1F1F23)
val MonoSlateDark = Color(0xFF2E2E34)
val MonoSlateMedium = Color(0xFF52525B)
val MonoSlateLight = Color(0xFF71717A)
val MonoSilver = Color(0xFFA1A1AA)
val MonoPlatinum = Color(0xFFD4D4D8)
val MonoPearl = Color(0xFFE4E4E7)
val MonoAlabaster = Color(0xFFF4F4F6)
val MonoPureWhite = Color(0xFFFFFFFF)

// Light Theme Specific Monochrome Tokens
val MonoLightBackground = Color(0xFFFAFAFA)
val MonoLightSurface = Color(0xFFFFFFFF)
val MonoLightSurfaceVariant = Color(0xFFF0F0F3)
val MonoLightSurfaceContainer = Color(0xFFF4F4F6)
val MonoLightSurfaceContainerHigh = Color(0xFFEBEBEF)
val MonoLightTextPrimary = Color(0xFF09090B)
val MonoLightTextSecondary = Color(0xFF52525B)
val MonoLightTextTertiary = Color(0xFF71717A)
val MonoLightOutline = Color(0xFFD4D4D8)
val MonoLightOutlineVariant = Color(0xFFE4E4E7)

// Dark Theme Specific Monochrome Tokens
val MonoDarkBackground = Color(0xFF09090B)
val MonoDarkSurface = Color(0xFF111114)
val MonoDarkSurfaceVariant = Color(0xFF1E1E22)
val MonoDarkSurfaceContainer = Color(0xFF18181C)
val MonoDarkSurfaceContainerHigh = Color(0xFF24242A)
val MonoDarkTextPrimary = Color(0xFFFAFAFA)
val MonoDarkTextSecondary = Color(0xFFA1A1AA)
val MonoDarkTextTertiary = Color(0xFF71717A)
val MonoDarkOutline = Color(0xFF3F3F46)
val MonoDarkOutlineVariant = Color(0xFF27272A)

// Standard M3 error palette (kept semantic for accessibility in a monochrome app)
val MonoLightError = Color(0xFFBA1A1A)
val MonoLightOnError = Color(0xFFFFFFFF)
val MonoLightErrorContainer = Color(0xFFFFDAD6)
val MonoLightOnErrorContainer = Color(0xFF410002)
val MonoDarkError = Color(0xFFFFB4AB)
val MonoDarkOnError = Color(0xFF690005)
val MonoDarkErrorContainer = Color(0xFF93000A)
val MonoDarkOnErrorContainer = Color(0xFFFFDAD6)

// =========================================================================
// Material 3 Expressive Monochrome Light Color Scheme
// Complete set of M3 color roles mapped to the tonal monochrome scale.
// =========================================================================
val LightColorScheme = lightColorScheme(
    primary = MonoPitchBlack,
    onPrimary = MonoPureWhite,
    primaryContainer = MonoPearl,
    onPrimaryContainer = MonoObsidian,
    inversePrimary = MonoPureWhite,
    secondary = MonoSlateMedium,
    onSecondary = MonoPureWhite,
    secondaryContainer = MonoAlabaster,
    onSecondaryContainer = MonoObsidian,
    tertiary = MonoSlateLight,
    onTertiary = MonoPureWhite,
    tertiaryContainer = MonoPearl,
    onTertiaryContainer = MonoCharcoal,
    background = MonoLightBackground,
    onBackground = MonoLightTextPrimary,
    surface = MonoLightSurface,
    onSurface = MonoLightTextPrimary,
    surfaceVariant = MonoLightSurfaceVariant,
    onSurfaceVariant = MonoLightTextSecondary,
    surfaceTint = MonoPitchBlack,
    inverseSurface = MonoGraphite,
    inverseOnSurface = MonoPureWhite,
    surfaceDim = MonoPlatinum,
    surfaceBright = MonoLightBackground,
    surfaceContainerLowest = MonoPureWhite,
    surfaceContainerLow = Color(0xFFF6F6F8),
    surfaceContainer = MonoLightSurfaceContainer,
    surfaceContainerHigh = MonoLightSurfaceContainerHigh,
    surfaceContainerHighest = MonoPlatinum,
    outline = MonoLightOutline,
    outlineVariant = MonoLightOutlineVariant,
    scrim = MonoPitchBlack,
    error = MonoLightError,
    onError = MonoLightOnError,
    errorContainer = MonoLightErrorContainer,
    onErrorContainer = MonoLightOnErrorContainer
)

// =========================================================================
// Material 3 Expressive Monochrome Dark Color Scheme
// =========================================================================
val DarkColorScheme = darkColorScheme(
    primary = MonoPureWhite,
    onPrimary = MonoPitchBlack,
    primaryContainer = MonoGraphite,
    onPrimaryContainer = MonoPureWhite,
    inversePrimary = MonoPitchBlack,
    secondary = MonoSilver,
    onSecondary = MonoPitchBlack,
    secondaryContainer = MonoCharcoal,
    onSecondaryContainer = MonoPlatinum,
    tertiary = MonoPlatinum,
    onTertiary = MonoPitchBlack,
    tertiaryContainer = MonoSlateDark,
    onTertiaryContainer = MonoPureWhite,
    background = MonoDarkBackground,
    onBackground = MonoDarkTextPrimary,
    surface = MonoDarkSurface,
    onSurface = MonoDarkTextPrimary,
    surfaceVariant = MonoDarkSurfaceVariant,
    onSurfaceVariant = MonoDarkTextSecondary,
    surfaceTint = MonoPureWhite,
    inverseSurface = MonoPureWhite,
    inverseOnSurface = MonoPitchBlack,
    surfaceDim = Color(0xFF0A0A0C),
    surfaceBright = Color(0xFF26262B),
    surfaceContainerLowest = MonoPitchBlack,
    surfaceContainerLow = MonoCharcoal,
    surfaceContainer = MonoDarkSurfaceContainer,
    surfaceContainerHigh = MonoDarkSurfaceContainerHigh,
    surfaceContainerHighest = Color(0xFF303036),
    outline = MonoDarkOutline,
    outlineVariant = MonoDarkOutlineVariant,
    scrim = MonoPitchBlack,
    error = MonoDarkError,
    onError = MonoDarkOnError,
    errorContainer = MonoDarkErrorContainer,
    onErrorContainer = MonoDarkOnErrorContainer
)

