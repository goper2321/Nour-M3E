package com.example.features.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.AdhanSettingsEntity
import com.example.database.NourDatabase
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.UserLocation
import com.example.ui.m3e.M3EAlertDialog
import com.example.ui.m3e.M3EButton
import com.example.ui.m3e.M3EButtonSize
import com.example.ui.m3e.M3EButtonVariant
import com.example.ui.m3e.M3ECard
import com.example.ui.m3e.M3ECardHeader
import com.example.ui.m3e.M3ECardVariant
import com.example.ui.m3e.M3EMenuField
import com.example.ui.m3e.M3ESectionHeader
import com.example.ui.m3e.M3ESpacing
import com.example.ui.m3e.M3ESwitchRow
import kotlinx.coroutines.launch

/**
 * M3E Settings — same preferences logic, rebuilt presentation layer.
 *
 * Every group is an m3e card with header/content slots:
 * filled (location/method/madhab/latitude/tuning/adhan) + tonal privacy footer.
 * Menus use the m3e-select port (M3EMenuField), toggles use M3ESwitchRow
 * (m3e-switch port), dialogs use M3EAlertDialog (m3e-dialog port).
 */
@Composable
fun SettingsScreen(
    location: UserLocation,
    calculationMethod: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments,
    onLocationChange: (UserLocation) -> Unit,
    onMethodChange: (CalculationMethod) -> Unit,
    onMadhabChange: (Madhab) -> Unit,
    onHighLatRuleChange: (HighLatitudeRule) -> Unit,
    onAdjustmentsChange: (PrayerAdjustments) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }

    val adhanSettingsList by db.adhanSettingsDao().getAllSettings().collectAsState(initial = emptyList())

    var showAdjustmentsDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var isTestingAdhan by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("settings_screen_list"),
            contentPadding = PaddingValues(M3ESpacing.md),
            verticalArrangement = Arrangement.spacedBy(M3ESpacing.md)
        ) {
            item {
                M3ESectionHeader(
                    title = "Settings & Preferences",
                    subtitle = "Calculation models, Adhan calls, and privacy."
                )
            }

            // Location
            item {
                M3ECard(
                    variant = M3ECardVariant.Filled,
                    header = {
                        M3ECardHeader(
                            eyebrow = "Geolocation",
                            title = "Active Location",
                            subtitle = "${location.cityName}, ${location.countryName}",
                            trailing = {
                                M3EButton(
                                    onClick = { showLocationDialog = true },
                                    size = M3EButtonSize.ExtraSmall
                                ) { Text("Change") }
                            }
                        )
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lat ${String.format("%.4f", location.latitude)}, Lng ${String.format("%.4f", location.longitude)} • GMT${if (location.timezoneOffsetHours >= 0) "+" else ""}${location.timezoneOffsetHours}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Calculation method (m3e-select)
            item {
                M3ECard(
                    variant = M3ECardVariant.Filled,
                    header = {
                        M3ECardHeader(
                            eyebrow = "Fiqh authority",
                            title = "Calculation Method",
                            subtitle = "Fajr/Isha angles per authority"
                        )
                    }
                ) {
                    M3EMenuField(
                        valueText = calculationMethod.title,
                        options = CalculationMethod.values().toList(),
                        onSelect = onMethodChange,
                        label = { it.title },
                        testTag = "settings_method_field"
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Fajr ${calculationMethod.fajrAngle}° • Isha ${if (calculationMethod.ishaIntervalMinutes != null) "+${calculationMethod.ishaIntervalMinutes} min" else "${calculationMethod.ishaAngle}°"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Madhab (m3e-switch rows)
            item {
                M3ECard(
                    variant = M3ECardVariant.Filled,
                    header = {
                        M3ECardHeader(
                            eyebrow = "Asr shadow",
                            title = "Madhab",
                            subtitle = "Asr shadow-length factor"
                        )
                    }
                ) {
                    Madhab.values().forEachIndexed { index, m ->
                        M3ESwitchRow(
                            title = m.title,
                            subtitle = if (m.shadowFactor == 1.0) "Shadow factor 1 (earlier Asr)" else "Shadow factor 2 (later Asr)",
                            checked = madhab == m,
                            onCheckedChange = { if (it) onMadhabChange(m) },
                            testTag = "settings_madhab_${m.name}"
                        )
                        if (index < Madhab.values().size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            // High latitude rule (m3e-select)
            item {
                M3ECard(
                    variant = M3ECardVariant.Filled,
                    header = {
                        M3ECardHeader(
                            eyebrow = "Extreme latitudes",
                            title = "High Latitude Rule",
                            subtitle = "Twilight handling for Scandinavia & beyond"
                        )
                    }
                ) {
                    M3EMenuField(
                        valueText = highLatRule.title,
                        options = HighLatitudeRule.values().toList(),
                        onSelect = onHighLatRuleChange,
                        label = { it.title },
                        testTag = "settings_highlat_field"
                    )
                }
            }

            // Per-prayer tuning
            item {
                M3ECard(
                    variant = M3ECardVariant.Filled,
                    header = {
                        M3ECardHeader(
                            eyebrow = "Mosque matching",
                            title = "Minute Adjustments",
                            subtitle = "Fajr ${adjustments.fajr}m • Dhuhr ${adjustments.dhuhr}m • Asr ${adjustments.asr}m • Maghrib ${adjustments.maghrib}m • Isha ${adjustments.isha}m",
                            trailing = {
                                M3EButton(
                                    onClick = { showAdjustmentsDialog = true },
                                    variant = M3EButtonVariant.Tonal,
                                    size = M3EButtonSize.ExtraSmall
                                ) { Text("Tune") }
                            }
                        )
                    }
                ) {
                    Text(
                        text = "Match your local mosque timetable to the minute. Sunrise offset ${adjustments.sunrise}m.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Adhan manager
            item {
                M3ECard(
                    variant = M3ECardVariant.Filled,
                    header = {
                        M3ECardHeader(
                            eyebrow = "Audio & alarms",
                            title = "Adhan Calls",
                            subtitle = "Exact alarms + per-prayer toggles",
                            trailing = {
                                M3EButton(
                                    onClick = {
                                        if (isTestingAdhan) {
                                            AdhanPlayer.stop()
                                            isTestingAdhan = false
                                        } else {
                                            isTestingAdhan = true
                                            AdhanPlayer.playAdhan(
                                                context = context,
                                                toneName = "Makkah Adhan",
                                                volume = 1.0f,
                                                onCompletion = { isTestingAdhan = false }
                                            )
                                        }
                                    },
                                    variant = M3EButtonVariant.Outlined,
                                    size = M3EButtonSize.ExtraSmall
                                ) { Text(if (isTestingAdhan) "Stop" else "Test audio") }
                            }
                        )
                    }
                ) {
                    val prayers = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
                    prayers.forEachIndexed { index, pName ->
                        val setting = adhanSettingsList.firstOrNull { it.prayerName == pName }
                        val isEnabled = setting?.isAdhanEnabled ?: true
                        M3ESwitchRow(
                            title = pName,
                            subtitle = setting?.toneName ?: "Makkah Adhan",
                            checked = isEnabled,
                            onCheckedChange = { checked ->
                                coroutineScope.launch {
                                    val newSetting = (setting ?: AdhanSettingsEntity(pName)).copy(isAdhanEnabled = checked)
                                    db.adhanSettingsDao().saveSettings(newSetting)
                                }
                            },
                            testTag = "adhan_toggle_$pName"
                        )
                        if (index < prayers.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            // Privacy pledge (tonal footer card)
            item {
                M3ECard(
                    variant = M3ECardVariant.Elevated,
                    header = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Nour Privacy Pledge",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    Text(
                        text = "• 100% offline-first architecture\n• Zero ads, analytics, or trackers\n• No accounts required\n• Local storage only (SQLite / Room)\n• Open & private Islamic companion",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showAdjustmentsDialog) {
        AdjustmentsDialog(
            adjustments = adjustments,
            onDismiss = { showAdjustmentsDialog = false },
            onSave = {
                onAdjustmentsChange(it)
                showAdjustmentsDialog = false
            }
        )
    }

    if (showLocationDialog) {
        LocationDialog(
            current = location,
            onDismiss = { showLocationDialog = false },
            onSelect = {
                onLocationChange(it)
                showLocationDialog = false
            }
        )
    }
}

/** Minute-tuning dialog (m3e-dialog port). */
@Composable
private fun AdjustmentsDialog(
    adjustments: PrayerAdjustments,
    onDismiss: () -> Unit,
    onSave: (PrayerAdjustments) -> Unit
) {
    var fajrAdj by remember { mutableStateOf(adjustments.fajr.toString()) }
    var sunriseAdj by remember { mutableStateOf(adjustments.sunrise.toString()) }
    var dhuhrAdj by remember { mutableStateOf(adjustments.dhuhr.toString()) }
    var asrAdj by remember { mutableStateOf(adjustments.asr.toString()) }
    var maghribAdj by remember { mutableStateOf(adjustments.maghrib.toString()) }
    var ishaAdj by remember { mutableStateOf(adjustments.isha.toString()) }

    M3EAlertDialog(
        onDismiss = onDismiss,
        title = "Tune prayer minutes",
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Adjust minutes to match your local mosque timetable:", fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fajrAdj, onValueChange = { fajrAdj = it }, label = { Text("Fajr") }, modifier = Modifier.weight(1f), singleLine = true, shape = MaterialTheme.shapes.medium)
                    OutlinedTextField(value = sunriseAdj, onValueChange = { sunriseAdj = it }, label = { Text("Sunrise") }, modifier = Modifier.weight(1f), singleLine = true, shape = MaterialTheme.shapes.medium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dhuhrAdj, onValueChange = { dhuhrAdj = it }, label = { Text("Dhuhr") }, modifier = Modifier.weight(1f), singleLine = true, shape = MaterialTheme.shapes.medium)
                    OutlinedTextField(value = asrAdj, onValueChange = { asrAdj = it }, label = { Text("Asr") }, modifier = Modifier.weight(1f), singleLine = true, shape = MaterialTheme.shapes.medium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = maghribAdj, onValueChange = { maghribAdj = it }, label = { Text("Maghrib") }, modifier = Modifier.weight(1f), singleLine = true, shape = MaterialTheme.shapes.medium)
                    OutlinedTextField(value = ishaAdj, onValueChange = { ishaAdj = it }, label = { Text("Isha") }, modifier = Modifier.weight(1f), singleLine = true, shape = MaterialTheme.shapes.medium)
                }
            }
        },
        confirm = {
            M3EButton(
                onClick = {
                    onSave(
                        PrayerAdjustments(
                            fajr = fajrAdj.toIntOrNull() ?: 0,
                            sunrise = sunriseAdj.toIntOrNull() ?: 0,
                            dhuhr = dhuhrAdj.toIntOrNull() ?: 0,
                            asr = asrAdj.toIntOrNull() ?: 0,
                            maghrib = maghribAdj.toIntOrNull() ?: 0,
                            isha = ishaAdj.toIntOrNull() ?: 0
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismiss = {
            M3EButton(onClick = onDismiss, variant = M3EButtonVariant.Text) { Text("Cancel") }
        }
    )
}

/** City picker dialog (m3e-dialog port with selectable list rows). */
@Composable
private fun LocationDialog(
    current: UserLocation,
    onDismiss: () -> Unit,
    onSelect: (UserLocation) -> Unit
) {
    val predefinedLocations = listOf(
        UserLocation(21.4225, 39.8262, "Makkah al-Mukarramah", "Saudi Arabia", 3.0),
        UserLocation(24.4672, 39.6111, "Madinah al-Munawwarah", "Saudi Arabia", 3.0),
        UserLocation(31.7761, 35.2358, "Al-Quds (Jerusalem)", "Palestine", 3.0),
        UserLocation(40.7128, -74.0060, "New York", "United States", -5.0),
        UserLocation(51.5074, -0.1278, "London", "United Kingdom", 0.0),
        UserLocation(48.8566, 2.3522, "Paris", "France", 1.0),
        UserLocation(30.0444, 31.2357, "Cairo", "Egypt", 2.0),
        UserLocation(33.5731, -7.5898, "Casablanca", "Morocco", 1.0),
        UserLocation(59.3293, 18.0686, "Stockholm", "Sweden", 1.0),
        UserLocation(3.1390, 101.6869, "Kuala Lumpur", "Malaysia", 8.0),
        UserLocation(-6.2088, 106.8456, "Jakarta", "Indonesia", 7.0),
        UserLocation(24.8607, 67.0011, "Karachi", "Pakistan", 5.0),
        UserLocation(25.2048, 55.2708, "Dubai", "UAE", 4.0),
        UserLocation(41.0082, 28.9784, "Istanbul", "Turkey", 3.0)
    )

    M3EAlertDialog(
        onDismiss = onDismiss,
        title = "Select city",
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(predefinedLocations, key = { it.cityName }) { loc ->
                    val selected = current.cityName == loc.cityName
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = { onSelect(loc) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${loc.cityName}, ${loc.countryName}",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirm = {
            M3EButton(onClick = onDismiss, variant = M3EButtonVariant.Text) { Text("Close") }
        }
    )
}
