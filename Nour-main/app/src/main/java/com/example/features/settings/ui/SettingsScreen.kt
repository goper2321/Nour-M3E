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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.UserLocation
import kotlinx.coroutines.launch

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }

    val adhanSettingsList by db.adhanSettingsDao().getAllSettings().collectAsState(initial = emptyList())

    var showAdjustmentsDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var isTestingAdhan by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Text(
                text = "Settings & Preferences",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Customize prayer calculation models, Adhan calls, and privacy.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Location & City
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Active Location", fontWeight = FontWeight.Bold)
                            Text(
                                text = "${location.cityName}, ${location.countryName}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Lat: ${String.format("%.4f", location.latitude)}, Lng: ${String.format("%.4f", location.longitude)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = { showLocationDialog = true }) {
                            Text("Change")
                        }
                    }
                }
            }
        }

        // Calculation Authority & Method
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Calculation Authority / Method", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    var methodMenuOpen by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { methodMenuOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = calculationMethod.title, maxLines = 1)
                    }

                    DropdownMenu(
                        expanded = methodMenuOpen,
                        onDismissRequest = { methodMenuOpen = false }
                    ) {
                        CalculationMethod.values().forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.title) },
                                onClick = {
                                    onMethodChange(method)
                                    methodMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Madhab & Asr Calculation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Madhab (Asr Shadow Calculation)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Madhab.values().forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = m.title, fontSize = 13.sp)
                            Switch(
                                checked = madhab == m,
                                onCheckedChange = { if (it) onMadhabChange(m) }
                            )
                        }
                    }
                }
            }
        }

        // High Latitude Rules
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "High Latitude Rule (Scandinavia & North)", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    var highLatMenuOpen by remember { mutableStateOf(false) }
                    OutlinedButton(
                        onClick = { highLatMenuOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(highLatRule.title)
                    }

                    DropdownMenu(
                        expanded = highLatMenuOpen,
                        onDismissRequest = { highLatMenuOpen = false }
                    ) {
                        HighLatitudeRule.values().forEach { rule ->
                            DropdownMenuItem(
                                text = { Text(rule.title) },
                                onClick = {
                                    onHighLatRuleChange(rule)
                                    highLatMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Per-Prayer Minute Tuning
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Per-Prayer Minute Adjustments", fontWeight = FontWeight.Bold)
                            Text(
                                text = "Fajr: ${adjustments.fajr}m, Dhuhr: ${adjustments.dhuhr}m, Asr: ${adjustments.asr}m, Maghrib: ${adjustments.maghrib}m, Isha: ${adjustments.isha}m",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(onClick = { showAdjustmentsDialog = true }) {
                            Text("Tune")
                        }
                    }
                }
            }
        }

        // Adhan & Notifications Sound Manager
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Adhan Calls & Exact Alarms", fontWeight = FontWeight.Bold)

                        OutlinedButton(
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
                            }
                        ) {
                            Text(if (isTestingAdhan) "Stop" else "Test Audio")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val prayers = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")
                    prayers.forEach { pName ->
                        val setting = adhanSettingsList.firstOrNull { it.prayerName == pName }
                        val isEnabled = setting?.isAdhanEnabled ?: true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = pName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(text = setting?.toneName ?: "Makkah Adhan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { checked ->
                                    coroutineScope.launch {
                                        val newSetting = (setting ?: AdhanSettingsEntity(pName)).copy(isAdhanEnabled = checked)
                                        db.adhanSettingsDao().saveSettings(newSetting)
                                    }
                                }
                            )
                        }
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // Privacy & Offline Audit Badge
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nour Privacy Pledge",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 100% Offline-First Architecture\n• Zero Advertising, Zero Analytics, Zero Trackers\n• No User Accounts Required\n• Local Storage Only (SQLite / Room)\n• Open & Private Islamic Companion",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }

    // Per-Prayer Minute Tuning Dialog
    if (showAdjustmentsDialog) {
        var fajrAdj by remember { mutableStateOf(adjustments.fajr.toString()) }
        var sunriseAdj by remember { mutableStateOf(adjustments.sunrise.toString()) }
        var dhuhrAdj by remember { mutableStateOf(adjustments.dhuhr.toString()) }
        var asrAdj by remember { mutableStateOf(adjustments.asr.toString()) }
        var maghribAdj by remember { mutableStateOf(adjustments.maghrib.toString()) }
        var ishaAdj by remember { mutableStateOf(adjustments.isha.toString()) }

        AlertDialog(
            onDismissRequest = { showAdjustmentsDialog = false },
            title = { Text("Tune Prayer Minutes (+/-)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Adjust minutes to match your local mosque timetable exactly:", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = fajrAdj, onValueChange = { fajrAdj = it }, label = { Text("Fajr") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = sunriseAdj, onValueChange = { sunriseAdj = it }, label = { Text("Sunrise") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = dhuhrAdj, onValueChange = { dhuhrAdj = it }, label = { Text("Dhuhr") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = asrAdj, onValueChange = { asrAdj = it }, label = { Text("Asr") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = maghribAdj, onValueChange = { maghribAdj = it }, label = { Text("Maghrib") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = ishaAdj, onValueChange = { ishaAdj = it }, label = { Text("Isha") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdjustmentsChange(
                            PrayerAdjustments(
                                fajr = fajrAdj.toIntOrNull() ?: 0,
                                sunrise = sunriseAdj.toIntOrNull() ?: 0,
                                dhuhr = dhuhrAdj.toIntOrNull() ?: 0,
                                asr = asrAdj.toIntOrNull() ?: 0,
                                maghrib = maghribAdj.toIntOrNull() ?: 0,
                                isha = ishaAdj.toIntOrNull() ?: 0
                            )
                        )
                        showAdjustmentsDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdjustmentsDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Location Dialog
    if (showLocationDialog) {
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

        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text("Select City") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(predefinedLocations.size) { index ->
                        val loc = predefinedLocations[index]
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (location.cityName == loc.cityName) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = {
                                    onLocationChange(loc)
                                    showLocationDialog = false
                                },
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
            confirmButton = {
                TextButton(onClick = { showLocationDialog = false }) { Text("Close") }
            }
        )
    }
}
