package com.example.ui.m3e

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/**
 * M3E App Bar — Compose port of `m3e-app-bar`.
 *
 * Web source: packages/web/src/app-bar (sizes small|medium|large, title+subtitle
 * slots, leading/trailing slots, elevation-on-scroll via `for` scroll container).
 *
 * Mapping:
 * - small  -> TopAppBar / CenterAlignedTopAppBar (centered=true default, like m3e)
 * - medium -> MediumTopAppBar (title + subtitle collapsing)
 * - large  -> LargeTopAppBar
 * Scroll elevation is delegated to [scrollBehavior] (nestedScroll on Scaffold).
 */
enum class M3EAppBarSize { Small, Medium, Large }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3EAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    size: M3EAppBarSize = M3EAppBarSize.Small,
    centered: Boolean = true,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    testTag: String? = null
) {
    val titleContent: @Composable () -> Unit = {
        Column {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = when (size) {
                    M3EAppBarSize.Small -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    M3EAppBarSize.Medium -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    M3EAppBarSize.Large -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                }
            )
            if (subtitle != null && size != M3EAppBarSize.Small) {
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    val subtitleSmall: @Composable (() -> Unit)? = if (subtitle != null && size == M3EAppBarSize.Small) {
        { Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    } else null

    // Small uses centered layout by default (m3e `centered` attr); medium/large are start-aligned.
    val tagged = if (testTag != null) modifier.testTag(testTag) else modifier
    when (size) {
        M3EAppBarSize.Small -> if (centered) {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        titleContent()
                        subtitleSmall?.invoke()
                    }
                },
                modifier = tagged,
                navigationIcon = navigationIcon ?: {},
                actions = actions,
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        } else {
            TopAppBar(
                title = {
                    Column {
                        titleContent()
                        subtitleSmall?.invoke()
                    }
                },
                modifier = tagged,
                navigationIcon = navigationIcon ?: {},
                actions = actions,
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
        M3EAppBarSize.Medium -> MediumTopAppBar(
            title = { titleContent() },
            modifier = tagged,
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.mediumTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )
        M3EAppBarSize.Large -> LargeTopAppBar(
            title = { titleContent() },
            modifier = tagged,
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        )
    }
}
