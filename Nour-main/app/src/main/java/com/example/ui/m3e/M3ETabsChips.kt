package com.example.ui.m3e

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * M3E tabs / chips — Compose port of `m3e-chips`, `m3e-segmented-button`,
 * `m3e-slide-group` and `m3e-tabs`.
 *
 * Web guidance: filter chips for categorical filtering (All/Meccan/Medinan),
 * segmented buttons for 2-4 mutually exclusive view modes (Today/7-Day/Monthly,
 * Verse Cards/Continuous). Active segment uses filled container + on-primary
 * content; inactive is transparent with on-surface-variant content.
 */

/** Pill tab row used for 3-4 destination switches (Salah timetable, Quran tabs). */
@Composable
fun M3EPillTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String? = null
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .testTagIf(testTagPrefix ?: "m3e_pill_tabs")
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = selectedIndex == index
            SegmentedButton(
                selected = selected,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = tabs.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    activeBorderColor = Color.Transparent,
                    inactiveBorderColor = Color.Transparent
                ),
                modifier = if (testTagPrefix != null) Modifier.testTag("${testTagPrefix}_$index") else Modifier,
                label = {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            )
        }
    }
}

/** Two-option expressive view-mode toggle (Verse Cards / Continuous). */
@Composable
fun M3EViewModeToggle(
    options: List<Pair<String, ImageVector?>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "m3e_view_mode_toggle",
    optionTestTags: List<String>? = null
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth().testTag(testTag)
    ) {
        options.forEachIndexed { index, (label, icon) ->
            val selected = selectedIndex == index
            SegmentedButton(
                selected = selected,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primary,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = Color.Transparent,
                    inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = if (optionTestTags != null && index < optionTestTags.size) {
                    Modifier.testTag(optionTestTags[index])
                } else Modifier,
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            )
        }
    }
}

/** Filter chip row for categorical filters with counts (m3e filter-chip-set). */
@Composable
fun M3EFilterChipRow(
    options: List<Pair<String, String?>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(M3ESpacing.xs)
    ) {
        options.forEach { (label, count) ->
            val isSelected = selected == label
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(label) },
                label = {
                    Text(
                        text = if (count != null) "$label ($count)" else label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

/** Small tonal badge (m3e badge + assist-chip pattern) e.g. "NEXT", "604p", "7 Days". */
@Composable
fun M3EBadge(
    text: String,
    modifier: Modifier = Modifier,
    tonal: Boolean = true
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (tonal) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        else MaterialTheme.colorScheme.primary,
        contentColor = if (tonal) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

/** Section header shared by all settings/preference groups (m3e heading + content-pane). */
@Composable
fun M3ESectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
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

private fun Modifier.testTagIf(tag: String): Modifier = this.testTag(tag)
