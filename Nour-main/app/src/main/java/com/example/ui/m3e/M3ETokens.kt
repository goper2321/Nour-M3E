package com.example.ui.m3e

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * M3E design tokens — Compose port of @m3e-main/web design language.
 *
 * Mirrors the web token structure:
 * - spacing rhythm (4dp baseline)
 * - expressive shape scale (mirrors m3e shape + AppBar/Button/Card shape tokens)
 * - elevation hierarchy (filled=0, outlined=0, elevated=1..3)
 * - expressive motion (spring specs used by nav-bar, button shape-morph, FAB, sheets)
 * - adaptive breakpoints (compact <600dp, medium 600..840dp, expanded >840dp,
 *   matching m3e nav-bar vs nav-rail vs drawer-container guidance)
 */
object M3ESpacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val sm: Dp = 12.dp
    val md: Dp = 16.dp
    val lg: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 48.dp

    val screenPadding: PaddingValues
        @Composable get() = PaddingValues(
            horizontal = md,
            vertical = md
        )

    val cardPadding: Dp = md
    val sectionSpacing: Dp = md
    val itemSpacing: Dp = xs
}

object M3EElevation {
    val filled: Dp = 0.dp
    val outlined: Dp = 0.dp
    val elevatedDefault: Dp = 1.dp
    val elevatedHover: Dp = 2.dp
    val elevatedPressed: Dp = 1.dp
    val dialog: Dp = 3.dp
    val sheet: Dp = 1.dp
    val fab: Dp = 3.dp
    val appBarOnScroll: Dp = 2.dp
}

object M3EMotion {
    /** Expressive spring used for nav selection, shape morph, FAB menu. */
    fun <T> expressiveSpring() = spring<T>(
        dampingRatio = 0.8f,
        stiffness = 380f
    )

    fun <T> fastSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    const val fadeDuration: Int = 200
    const val expandDuration: Int = 300
}

enum class M3EWindowSize { Compact, Medium, Expanded }

@Composable
fun rememberM3EWindowSize(maxWidth: Dp): M3EWindowSize = when {
    maxWidth < 600.dp -> M3EWindowSize.Compact
    maxWidth < 840.dp -> M3EWindowSize.Medium
    else -> M3EWindowSize.Expanded
}

/** Touch target floor per M3E a11y guidance (48dp). */
val M3EMinTouchTarget: Dp = 48.dp

/** Shared content alphas matching m3e state-layer conventions. */
object M3EAlpha {
    const val subtle = 0.08f
    const val hover = 0.08f
    const val focus = 0.12f
    const val pressed = 0.12f
    const val outlineMuted = 0.35f
    const val scrimHero = 0.28f
}

/** Typography helpers: eyebrow label used across cards/headers (m3e heading + badge pattern). */
@Composable
fun m3eEyebrowStyle() = MaterialTheme.typography.labelLarge.copy(
    color = MaterialTheme.colorScheme.primary
)
