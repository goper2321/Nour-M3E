package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.MyApplicationTheme
import java.util.Date

enum class NourNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    SALAH("Salah", Icons.Filled.Notifications, Icons.Outlined.Notifications, "nav_salah"),
    QIBLA("Qibla", Icons.Filled.LocationOn, Icons.Outlined.LocationOn, "nav_qibla"),
    QURAN("Qur'an", Icons.Filled.DateRange, Icons.Outlined.DateRange, "nav_quran"),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings")
}

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

@Composable
fun NourMainApp() {
    val context = LocalContext.current

    var currentDestination by remember { mutableStateOf(NourNavDestination.SALAH) }

    // User Prayer Settings state
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

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("nour_main_scaffold"),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nour_bottom_navigation"),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 0.dp
            ) {
                NourNavDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(destination.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentDestination,
                transitionSpec = {
                    val enter = scaleIn(
                        initialScale = 0.92f,
                        animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f)
                    ) + fadeIn(
                        animationSpec = spring(dampingRatio = 1f, stiffness = 1600f)
                    )
                    val exit = fadeOut(
                        animationSpec = spring(dampingRatio = 1f, stiffness = 1600f)
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
