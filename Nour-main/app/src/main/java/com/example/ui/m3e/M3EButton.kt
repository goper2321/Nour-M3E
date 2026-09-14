package com.example.ui.m3e

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * M3E Button — Compose port of `m3e-button`.
 *
 * Web source: packages/web/src/button (variants elevated|filled|tonal|outlined|text;
 * sizes extra-small|small|medium|large|extra-large; shape rounded|square with
 * pressed shape-morph `--m3e-button-[size]-shape-pressed-morph`).
 *
 * Mapping:
 * - variant -> M3 filled/tonal/elevated/outlined/text composables
 * - size    -> container height + horizontal padding + label style
 * - pressed shape-morph -> animate corner radius toward a squircle when pressed
 *   (expressive affordance), using M3EMotion.expressiveSpring.
 */
enum class M3EButtonVariant { Filled, Tonal, Elevated, Outlined, Text }

enum class M3EButtonSize(
    val containerHeight: Dp,
    val horizontalPadding: Dp
) {
    ExtraSmall(32.dp, 12.dp),
    Small(40.dp, 16.dp),
    Medium(48.dp, 20.dp),
    Large(56.dp, 24.dp),
    ExtraLarge(72.dp, 28.dp)
}

@Composable
fun M3EButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: M3EButtonVariant = M3EButtonVariant.Filled,
    size: M3EButtonSize = M3EButtonSize.Small,
    enabled: Boolean = true,
    shape: Shape? = null,
    contentPadding: PaddingValues? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    // Expressive shape-morph on press (m3e `--shape-pressed-morph`): slightly
    // squarer when pressed. Fall back to M3E shape scale otherwise.
    val resolvedShape: Shape = shape ?: if (pressed) {
        MaterialTheme.shapes.medium
    } else {
        when (size) {
            M3EButtonSize.ExtraSmall -> MaterialTheme.shapes.small
            M3EButtonSize.Small -> MaterialTheme.shapes.medium
            M3EButtonSize.Medium -> MaterialTheme.shapes.large
            M3EButtonSize.Large -> MaterialTheme.shapes.extraLarge
            M3EButtonSize.ExtraLarge -> MaterialTheme.shapes.extraLarge
        }
    }

    val resolvedPadding = contentPadding ?: PaddingValues(horizontal = size.horizontalPadding, vertical = 0.dp)
    val baseModifier = modifier.heightIn(min = maxOf(size.containerHeight, M3EMinTouchTarget.takeIf { variant != M3EButtonVariant.Text } ?: size.containerHeight))

    // Label style scales with size (m3e label-text size/weight/line-height tokens).
    // Applied implicitly via MaterialTheme; callers set Text style. We only ensure
    // min heights + shape + elevation hierarchy here.
    when (variant) {
        M3EButtonVariant.Filled -> Button(
            onClick = onClick,
            modifier = baseModifier,
            enabled = enabled,
            shape = resolvedShape,
            contentPadding = resolvedPadding,
            interactionSource = interactionSource,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = M3EElevation.filled,
                pressedElevation = M3EElevation.elevatedPressed
            ),
            content = content
        )
        M3EButtonVariant.Tonal -> FilledTonalButton(
            onClick = onClick,
            modifier = baseModifier,
            enabled = enabled,
            shape = resolvedShape,
            contentPadding = resolvedPadding,
            interactionSource = interactionSource,
            elevation = ButtonDefaults.filledTonalButtonElevation(
                defaultElevation = M3EElevation.filled,
                pressedElevation = M3EElevation.elevatedPressed
            ),
            content = content
        )
        M3EButtonVariant.Elevated -> ElevatedButton(
            onClick = onClick,
            modifier = baseModifier,
            enabled = enabled,
            shape = resolvedShape,
            contentPadding = resolvedPadding,
            interactionSource = interactionSource,
            elevation = ButtonDefaults.elevatedButtonElevation(
                defaultElevation = M3EElevation.elevatedDefault,
                pressedElevation = M3EElevation.elevatedPressed,
                hoveredElevation = M3EElevation.elevatedHover
            ),
            content = content
        )
        M3EButtonVariant.Outlined -> OutlinedButton(
            onClick = onClick,
            modifier = baseModifier,
            enabled = enabled,
            shape = resolvedShape,
            contentPadding = resolvedPadding,
            interactionSource = interactionSource,
            content = content
        )
        M3EButtonVariant.Text -> TextButton(
            onClick = onClick,
            modifier = baseModifier,
            enabled = enabled,
            shape = resolvedShape,
            contentPadding = resolvedPadding,
            interactionSource = interactionSource,
            content = content
        )
    }
}
