package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.features.adhan.scheduler.PrayerNotificationScheduler
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import com.example.features.prayer.ui.PrayerScreen
import com.example.features.qibla.ui.QiblaScreen
import com.example.features.quran.ui.QuranScreen
import com.example.features.settings.ui.SettingsScreen
import com.example.ui.m3e.M3EAppBar
import com.example.ui.m3e.M3EAppBarSize
import com.example.ui.m3e.M3EDrawerHeadline
import com.example.ui.m3e.M3ENavBar
import com.example.ui.m3e.M3ENavDrawerContent
import com.example.ui.m3e.M3ENavItem
import com.example.ui.m3e.M3ENavRail
import com.example.ui.m3e.M3EWindowSize
import com.example.ui.m3e.rememberM3EWindowSize
import com.example.ui.theme.MyApplicationTheme
import java.util.Date

/**
 * M3E app shell — port of m3e nav-bar / nav-rail / drawer-container + app-bar.
 *
 * - Compact (<600dp): small centered app bar + bottom nav bar (3-5 destinations)
 * - Medium (600-840dp): small app bar + side nav rail
 * - Expanded (>840dp): small app bar + permanent navigation drawer
 * Screen transitions use the shared expressive spring (m3e motion scheme).
 */
enum class NourNavDestination(
    val route: String,
    val title: String,
    val subtitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SALAH(
        "salah", "Salah Times", "Prayer timetable & countdown",
        Icons.Filled.Schedule, Icons.Outlined.Schedule, "nav_salah"
    ),
    QIBLA(
        "qibla", "Qibla", "Direction & distance",
        Icons.Filled.Explore, Icons.Outlined.Explore, "nav_qibla"
    ),
    QURAN(
        "quran", "The Noble Qur'an", "Surahs • Mushaf • Saved",
        Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_quran"
    ),
    SETTINGS(
        "settings", "Settings", "Calculation & Adhan",
        Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings"
    )
}

private fun NourNavDestination.toM3E(): M3ENavItem = M3ENavItem(
    route = route,
    label = if (route == "quran") "Qur'an" else title.substringBefore(" "),
    selectedIcon = selectedIcon,
    unselectedIcon = unselectedIcon,
    testTag = testTag
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NourMainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NourMainApp() {
    val context = LocalContext.current

    var currentDestination by remember { mutableStateOf(NourNavDestination.SALAH) }

    // User Prayer Settings state (business logic preserved verbatim)
    var userLocation by remember {
        mutableStateOf(UserLocation(21.4225, 39.8262, "Makkah", "Saudi Arabia", 3.0))
    }
    var calculationMethod by remember { mutableStateOf(CalculationMethod.MUSLIM_WORLD_LEAGUE) }
    var madhab by remember { mutableStateOf(Madhab.SHAFI_MALIKI_HANBALI) }
    var highLatRule by remember { mutableStateOf(HighLatitudeRule.ANGLE_BASED) }
    var adjustments by remember { mutableStateOf(PrayerAdjustments()) }

    // Schedule exact alarms for today's prayers on launch
    LaunchedEffect(userLocation, calculationMethod, madhab, adjustments) {
        val todayTimes = PrayerCalculationEngine.calculate(
            date = Date(),
            location = userLocation,
            method = calculationMethod,
            madhab = madhab,
            highLatRule = highLatRule,
            adjustments = adjustments
        )

        PrayerName.values().filter { it != PrayerName.SUNRISE }.forEach { p ->
            val timeMillis = todayTimes.getTimeFor(p).time
            if (timeMillis > System.currentTimeMillis()) {
                PrayerNotificationScheduler.scheduleExactAlarm(context, p, timeMillis, isPreReminder = false)
            }
        }
    }

    val navItems = remember { NourNavDestination.values().map { it.toM3E() } }
    val onSelectNav: (M3ENavItem) -> Unit = { item ->
        NourNavDestination.values().firstOrNull { it.route == item.route }?.let {
            currentDestination = it
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowSize = rememberM3EWindowSize(maxWidth)
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

        // Quran manages its own reader chrome; shell app bar stays for top-level
        // destinations and collapses gracefully via pinned scroll behavior.
        val appBarSubtitle = when (currentDestination) {
            NourNavDestination.SALAH -> "${userLocation.cityName} • ${calculationMethod.title.substringBefore(" (")}"
            NourNavDestination.QIBLA -> "${userLocation.cityName} → Kaaba, Makkah"
            NourNavDestination.QURAN -> currentDestination.subtitle
            NourNavDestination.SETTINGS -> currentDestination.subtitle
        }

        val content: @Composable () -> Unit = {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    val enter = scaleIn(
                        initialScale = 0.96f,
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 0.8f, stiffness = 380f
                        )
                    ) + fadeIn(
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 1f, stiffness = 1600f
                        )
                    )
                    val exit = fadeOut(
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 1f, stiffness = 1600f
                        )
                    )
                    enter togetherWith exit
                },
                label = "navigation_screen_transition"
            ) { destination ->
                when (destination) {
                    NourNavDestination.SALAH -> {
                        PrayerScreen(
                            location = userLocation,
                            calculationMethod = calculationMethod,
                            madhab = madhab,
                            highLatRule = highLatRule,
                            adjustments = adjustments,
                            onOpenAdjustments = { currentDestination = NourNavDestination.SETTINGS },
                            onOpenSettings = { currentDestination = NourNavDestination.SETTINGS }
                        )
                    }
                    NourNavDestination.QIBLA -> {
                        QiblaScreen(location = userLocation)
                    }
                    NourNavDestination.QURAN -> {
                        QuranScreen()
                    }
                    NourNavDestination.SETTINGS -> {
                        SettingsScreen(
                            location = userLocation,
                            calculationMethod = calculationMethod,
                            madhab = madhab,
                            highLatRule = highLatRule,
                            adjustments = adjustments,
                            onLocationChange = { userLocation = it },
                            onMethodChange = { calculationMethod = it },
                            onMadhabChange = { madhab = it },
                            onHighLatRuleChange = { highLatRule = it },
                            onAdjustmentsChange = { adjustments = it }
                        )
                    }
                }
            }
        }

        when (windowSize) {
            M3EWindowSize.Expanded -> {
                PermanentNavigationDrawer(
                    drawerContent = {
                        M3ENavDrawerContent(
                            items = navItems,
                            selectedRoute = currentDestination.route,
                            onSelect = onSelectNav,
                            headline = {
                                M3EDrawerHeadline(
                                    title = "Nour • نور",
                                    subtitle = "Islamic Companion"
                                )
                            },
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                ) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .testTag("nour_main_scaffold"),
                        topBar = {
                            M3EAppBar(
                                title = currentDestination.title,
                                subtitle = appBarSubtitle,
                                size = M3EAppBarSize.Small,
                                scrollBehavior = scrollBehavior,
                                testTag = "nour_top_app_bar"
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) { content() }
                    }
                }
            }
            M3EWindowSize.Medium -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    M3ENavRail(
                        items = navItems,
                        selectedRoute = currentDestination.route,
                        onSelect = onSelectNav,
                        header = {
                            Text(
                                text = "ن",
                                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    )
                    Scaffold(
                        modifier = Modifier
                            .weight(1f)
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .testTag("nour_main_scaffold"),
                        topBar = {
                            M3EAppBar(
                                title = currentDestination.title,
                                subtitle = appBarSubtitle,
                                size = M3EAppBarSize.Small,
                                scrollBehavior = scrollBehavior,
                                testTag = "nour_top_app_bar"
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) { content() }
                    }
                }
            }
            M3EWindowSize.Compact -> {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .testTag("nour_main_scaffold"),
                    topBar = {
                        M3EAppBar(
                            title = currentDestination.title,
                            subtitle = appBarSubtitle,
                            size = M3EAppBarSize.Small,
                            scrollBehavior = scrollBehavior,
                            testTag = "nour_top_app_bar"
                        )
                    },
                    bottomBar = {
                        M3ENavBar(
                            items = navItems,
                            selectedRoute = currentDestination.route,
                            onSelect = onSelectNav,
                            modifier = Modifier.testTag("nour_bottom_navigation")
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) { content() }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Nour - $name", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}
