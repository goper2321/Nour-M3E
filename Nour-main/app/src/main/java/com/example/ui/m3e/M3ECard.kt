package com.example.ui.m3e

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * M3E Card — Compose port of `m3e-card`.
 *
 * Web source: packages/web/src/card (variants filled|outlined|elevated;
 * orientation vertical|horizontal; slots header|content|actions|footer;
 * hover/focus/press state layers + elevation transitions).
 *
 * This wrapper enforces the slot structure instead of free-form content so all
 * Nour surfaces share one hierarchy: eyebrow -> title -> subtitle (header),
 * body (content), controls (actions), footnote (footer).
 */
enum class M3ECardVariant { Filled, Outlined, Elevated }

@Composable
fun M3ECard(
    modifier: Modifier = Modifier,
    variant: M3ECardVariant = M3ECardVariant.Filled,
    shape: Shape? = null,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit = {},
    actions: @Composable (ColumnScope.() -> Unit)? = null,
    footer: @Composable (ColumnScope.() -> Unit)? = null
) {
    val resolvedShape = shape ?: MaterialTheme.shapes.extraLarge
    val colors = when (variant) {
        M3ECardVariant.Filled -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
        M3ECardVariant.Outlined -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
        M3ECardVariant.Elevated -> CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }
    val border = when (variant) {
        M3ECardVariant.Outlined -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = M3EAlpha.outlineMuted))
        M3ECardVariant.Filled -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
        M3ECardVariant.Elevated -> null
    }
    val elevation = when (variant) {
        M3ECardVariant.Elevated -> CardDefaults.cardElevation(
            defaultElevation = M3EElevation.elevatedDefault,
            hoveredElevation = M3EElevation.elevatedHover,
            pressedElevation = M3EElevation.elevatedPressed
        )
        else -> CardDefaults.cardElevation(defaultElevation = M3EElevation.filled)
    }

    var cardModifier = modifier
    if (testTag != null) cardModifier = cardModifier.testTag(testTag)
    if (onClick != null) cardModifier = cardModifier.clip(resolvedShape).clickable(onClick = onClick)

    Card(
        modifier = cardModifier,
        shape = resolvedShape,
        colors = colors,
        border = border,
        elevation = elevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(M3ESpacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(M3ESpacing.xs)
        ) {
            if (header != null) {
                header()
                if (actions != null || footer != null) {
                    // content divider handled by caller when needed
                }
            }
            content()
            if (actions != null) {
                actions()
            }
            if (footer != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                footer()
            }
        }
    }
}

/**
 * Standard M3E card header: eyebrow (m3e heading slot) + title + subtitle,
 * with optional trailing slot (badge, icon-button, switch).
 */
@Composable
fun M3ECardHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) trailing()
    }
}

/** Tonal container color helper for hero/preview cards (keeps one hierarchy). */
@Composable
fun m3eHeroContainerColor(): Color = MaterialTheme.colorScheme.primaryContainer

@Composable
fun m3eOnHeroContainerColor(): Color = MaterialTheme.colorScheme.onPrimaryContainer
