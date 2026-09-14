package com.example.ui.m3e

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * M3E navigation — Compose port of `m3e-nav-bar`, `m3e-nav-rail`,
 * `m3e-nav-menu` and `m3e-drawer-container`.
 *
 * Web guidance:
 * - nav-bar: 3-5 vertical items on compact screens, pill active indicator
 *   (active container color, active icon/label colors, inactive muted).
 * - nav-rail: same items vertically on medium screens.
 * - drawer-container/nav-menu: full labels + icons on expanded screens.
 * All three share one [M3ENavItem] model so destinations stay consistent.
 */
data class M3ENavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
    val badgeCount: Int? = null
)

@Composable
private fun M3ENavIcon(
    item: M3ENavItem,
    selected: Boolean
) {
    Icon(
        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
        contentDescription = item.label
    )
}

/** Compact navigation bar (m3e-nav-bar, mode=compact). */
@Composable
fun M3ENavBar(
    items: List<M3ENavItem>,
    selectedRoute: String,
    onSelect: (M3ENavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .testTag("m3e_nav_bar"),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ) {
        items.forEach { item ->
            val selected = selectedRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(item) },
                icon = { M3ENavIcon(item, selected) },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

/** Medium navigation rail (m3e-nav-rail). */
@Composable
fun M3ENavRail(
    items: List<M3ENavItem>,
    selectedRoute: String,
    onSelect: (M3ENavItem) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (androidx.compose.foundation.layout.ColumnScope.() -> Unit)? = null
) {
    NavigationRail(
        modifier = modifier.testTag("m3e_nav_rail"),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        header = header
    ) {
        items.forEach { item ->
            val selected = selectedRoute == item.route
            NavigationRailItem(
                selected = selected,
                onClick = { onSelect(item) },
                icon = { M3ENavIcon(item, selected) },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

/** Expanded drawer content (m3e-drawer-container + m3e-nav-menu). */
@Composable
fun M3ENavDrawerContent(
    items: List<M3ENavItem>,
    selectedRoute: String,
    onSelect: (M3ENavItem) -> Unit,
    modifier: Modifier = Modifier,
    headline: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        if (headline != null) {
            headline()
            Spacer(modifier = Modifier.height(12.dp))
        }
        items.forEach { item ->
            val selected = selectedRoute == item.route
            NavigationDrawerItem(
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.label,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        if (item.badgeCount != null && item.badgeCount > 0) {
                            M3EBadge(text = "${item.badgeCount}")
                        }
                    }
                },
                icon = { M3ENavIcon(item, selected) },
                selected = selected,
                onClick = { onSelect(item) },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag(item.testTag),
                shape = MaterialTheme.shapes.large,
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/** Drawer headline: app wordmark + edition (m3e heading pattern). */
@Composable
fun M3EDrawerHeadline(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(40.dp)
        ) {
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                Text(text = "ن", fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
