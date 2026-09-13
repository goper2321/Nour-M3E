package com.example.features.prayer.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.NourDatabase
import com.example.database.PrayerLogEntity
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.calculation.HijriDateHelper
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import com.example.features.widgets.CelestialPrayerHeroCard
import com.example.features.widgets.ImmersiveSkyBackground
import com.example.ui.theme.MonoPearl
import com.example.ui.theme.MonoPlatinum
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun PrayerScreen(
    location: UserLocation,
    calculationMethod: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments,
    onOpenAdjustments: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { NourDatabase.getInstance(context) }

    var currentTime by remember { mutableStateOf(Date()) }
    // Update live ticker every second for precise countdown & active window
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(1000L)
        }
    }

    val todayPrayerTimes = remember(currentTime, location, calculationMethod, madhab, highLatRule, adjustments) {
        PrayerCalculationEngine.calculate(
            date = currentTime,
            location = location,
            method = calculationMethod,
            madhab = madhab,
            highLatRule = highLatRule,
            adjustments = adjustments
        )
    }

    val countdownState = todayPrayerTimes.getCountdownState(currentTime)
    val hijriDate = remember(currentTime) { HijriDateHelper.fromGregorian(currentTime) }

    val todayStr = remember(currentTime) { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(currentTime) }
    val prayerLogs by db.prayerLogDao().getLogsForDate(todayStr).collectAsState(initial = emptyList())

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Today, 1 = Weekly, 2 = Monthly, 3 = Solstices
    var playingAdhanPrayer by remember { mutableStateOf<PrayerName?>(null) }

    // Stop adhan if composable leaves composition
    DisposableEffect(Unit) {
        onDispose {
            if (AdhanPlayer.isPlaying) {
                AdhanPlayer.stop()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Immersive Atmospheric Sky Background spanning the full screen
        ImmersiveSkyBackground(
            prayerTimes = todayPrayerTimes,
            now = currentTime,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Foreground Weather App Style Layered Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("prayer_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Simple 2-Line Upper Card: Hijri Date & Equivalent Gregorian Date + Current Zone
            item {
                val gregorianDate = remember(currentTime) {
                    SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(currentTime)
                }
                val currentZone = remember(currentTime) {
                    val tz = TimeZone.getDefault()
                    val isDst = tz.inDaylightTime(currentTime)
                    val shortCode = tz.getDisplayName(isDst, TimeZone.SHORT, Locale.getDefault())
                    "${tz.id} ($shortCode)"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prayer_header_card"),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f)
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Line 1: Hijri Date (English & Arabic)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = hijriDate.formattedEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = hijriDate.formattedAr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4D4D8)
                            )
                        }

                        // Line 2: Gregorian Date equivalent + Current ZONE
                        Text(
                            text = "$gregorianDate • $currentZone",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            // Unified Aesthetic Celestial Prayer Hero Card with Dynamic Arc & Segmented Countdown
            item {
                CelestialPrayerHeroCard(
                    prayerTimes = todayPrayerTimes,
                    countdownState = countdownState,
                    now = currentTime
                )
            }

            // Modern Expressive Segmented Pill Tabs (Today, 7-Day, Monthly, Solstices)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PrayerTabPill(
                        title = "Today",
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    PrayerTabPill(
                        title = "7-Day",
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                    PrayerTabPill(
                        title = "Monthly",
                        isSelected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.weight(1f)
                    )
                    PrayerTabPill(
                        title = "Solstices",
                        isSelected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Today's View: Daily Prayer Tracking Progress Bar & Quick Log Capsules
                val obligatoryPrayers = listOf(
                    PrayerName.FAJR,
                    PrayerName.DHUHR,
                    PrayerName.ASR,
                    PrayerName.MAGHRIB,
                    PrayerName.ISHA
                )
                val prayedCount = obligatoryPrayers.count { p ->
                    prayerLogs.any { it.prayerName == p.name && it.status == "PRAYED" }
                }

                item {
                    DailyPrayerProgressCard(
                        prayedCount = prayedCount,
                        totalPrayers = 5,
                        prayerLogs = prayerLogs,
                        onTogglePrayer = { prayer ->
                            coroutineScope.launch {
                                val currentLog = prayerLogs.firstOrNull { it.prayerName == prayer.name }
                                val isPrayed = currentLog?.status == "PRAYED"
                                val newStatus = if (isPrayed) "MISSED" else "PRAYED"
                                db.prayerLogDao().insertOrUpdateLog(
                                    PrayerLogEntity(
                                        date = todayStr,
                                        prayerName = prayer.name,
                                        status = newStatus
                                    )
                                )
                                val msg = if (!isPrayed) "${prayer.displayName} marked as prayed" else "${prayer.displayName} unmarked"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // Today's Individual Prayer Cards
                items(PrayerName.values()) { prayer ->
                    val isNext = countdownState.nextPrayer == prayer
                    val isCurrent = countdownState.currentPrayer == prayer
                    val isSunrise = prayer == PrayerName.SUNRISE
                    val log = prayerLogs.firstOrNull { it.prayerName == prayer.name }
                    val isPrayed = log?.status == "PRAYED"
                    val isPlayingThis = playingAdhanPrayer == prayer && AdhanPlayer.isPlaying

                    PrayerCard(
                        prayer = prayer,
                        timeFormatted = todayPrayerTimes.formattedTime(prayer),
                        isNext = isNext,
                        isCurrent = isCurrent,
                        isSunrise = isSunrise,
                        isPrayed = isPrayed,
                        isPlayingAdhan = isPlayingThis,
                        onPlayAdhanToggle = {
                            if (isPlayingThis) {
                                AdhanPlayer.stop()
                                playingAdhanPrayer = null
                            } else {
                                playingAdhanPrayer = prayer
                                AdhanPlayer.playAdhan(
                                    context = context,
                                    toneName = "Makkah Adhan",
                                    volume = 1.0f,
                                    onCompletion = {
                                        playingAdhanPrayer = null
                                    }
                                )
                                Toast.makeText(context, "Playing Adhan preview for ${prayer.displayName}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onTogglePrayed = {
                            coroutineScope.launch {
                                val newStatus = if (isPrayed) "MISSED" else "PRAYED"
                                db.prayerLogDao().insertOrUpdateLog(
                                    PrayerLogEntity(
                                        date = todayStr,
                                        prayerName = prayer.name,
                                        status = newStatus
                                    )
                                )
                            }
                        }
                    )
                }
            }
            1 -> {
                // Weekly Schedule (7 Days)
                item {
                    WeeklyScheduleCard(
                        location = location,
                        method = calculationMethod,
                        madhab = madhab,
                        highLatRule = highLatRule,
                        adjustments = adjustments,
                        currentDate = currentTime
                    )
                }
            }
            2 -> {
                // Monthly Calendar
                item {
                    MonthlyCalendarCard(
                        location = location,
                        method = calculationMethod,
                        madhab = madhab,
                        highLatRule = highLatRule,
                        adjustments = adjustments,
                        currentDate = currentTime
                    )
                }
            }
            3 -> {
                // Astronomical Solstices & Equinoxes
                item {
                    YearAheadPreviewCard(
                        location = location,
                        method = calculationMethod,
                        madhab = madhab,
                        highLatRule = highLatRule,
                        adjustments = adjustments
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
}

@Composable
fun PrayerTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = if (isSelected) Color.White else Color.Transparent,
        contentColor = if (isSelected) Color.Black else Color.White.copy(alpha = 0.85f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

/**
 * Daily Prayer tracker showing completed obligations with individual touch capsules.
 */
@Composable
fun DailyPrayerProgressCard(
    prayedCount: Int,
    totalPrayers: Int,
    prayerLogs: List<PrayerLogEntity>,
    onTogglePrayer: (PrayerName) -> Unit
) {
    val progress = (prayedCount.toFloat() / totalPrayers).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Salah Tracking",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$prayedCount of $totalPrayers obligatory prayers completed",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Surface(
                    color = if (prayedCount == 5) Color.White else Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(100.dp),
                    border = if (prayedCount == 5) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Text(
                        text = if (prayedCount == 5) "Alhamdulillah • All 5" else "${(progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (prayedCount == 5) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.18f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 5 Interactive Quick-Toggle Capsules
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    PrayerName.FAJR to "Fajr",
                    PrayerName.DHUHR to "Dhuhr",
                    PrayerName.ASR to "Asr",
                    PrayerName.MAGHRIB to "Maghrib",
                    PrayerName.ISHA to "Isha"
                ).forEach { (prayer, label) ->
                    val isPrayed = prayerLogs.any { it.prayerName == prayer.name && it.status == "PRAYED" }
                    Surface(
                        onClick = { onTogglePrayer(prayer) },
                        shape = RoundedCornerShape(100.dp),
                        color = if (isPrayed) Color.White else Color.Black.copy(alpha = 0.35f),
                        border = if (isPrayed) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isPrayed) Icons.Default.Check else Icons.Outlined.Circle,
                                contentDescription = null,
                                tint = if (isPrayed) Color.Black else Color.White.copy(alpha = 0.45f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isPrayed) FontWeight.Bold else FontWeight.Normal,
                                color = if (isPrayed) Color.Black else Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Polished, high-craft individual prayer card with celestial iconography,
 * distinct Next/Current badges, audio preview triggers, and smooth check logging.
 */
@Composable
fun PrayerCard(
    prayer: PrayerName,
    timeFormatted: String,
    isNext: Boolean,
    isCurrent: Boolean,
    isSunrise: Boolean,
    isPrayed: Boolean,
    isPlayingAdhan: Boolean,
    onPlayAdhanToggle: () -> Unit,
    onTogglePrayed: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isNext -> Color.White
            isCurrent -> MonoPlatinum
            else -> Color.White.copy(alpha = 0.16f)
        },
        label = "card_border_color"
    )

    val containerColor = when {
        isNext -> MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
        isCurrent -> MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f)
        isSunrise -> MaterialTheme.colorScheme.scrim.copy(alpha = 0.26f)
        else -> MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f)
    }

    val prayerIcon: ImageVector = when (prayer) {
        PrayerName.FAJR -> Icons.Default.WbTwilight
        PrayerName.SUNRISE -> Icons.Default.WbSunny
        PrayerName.DHUHR -> Icons.Default.WbSunny
        PrayerName.ASR -> Icons.Default.WbSunny
        PrayerName.MAGHRIB -> Icons.Default.WbTwilight
        PrayerName.ISHA -> Icons.Default.NightsStay
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_card_${prayer.name}"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isNext || isCurrent) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Checkmark for logging prayer (or subtle icon for sunrise)
                if (!isSunrise) {
                    IconButton(
                        onClick = onTogglePrayed,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("log_prayer_${prayer.name}")
                    ) {
                        Icon(
                            imageVector = if (isPrayed) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = if (isPrayed) "Prayed" else "Mark as prayed",
                            tint = if (isPrayed) Color.White else Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                } else {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = prayerIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                // Prayer Title & Badges
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isPrayed) Color.White.copy(alpha = 0.65f) else Color.White
                        )

                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = "NEXT",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MonoPearl,
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = "CURRENT",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isSunrise) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color.White.copy(alpha = 0.20f),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = "SHURUQ",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.arabicName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MonoPlatinum,
                            fontWeight = FontWeight.Medium
                        )
                        if (isSunrise) {
                            Text(
                                text = " • Tahajjud concludes",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.70f),
                                fontSize = 11.sp
                            )
                        } else if (isPrayed) {
                            Text(
                                text = " • Completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Right side: Audio Preview & Formatted Time
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (!isSunrise) {
                    IconButton(
                        onClick = onPlayAdhanToggle,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlayingAdhan) Color.White else Color.White.copy(alpha = 0.15f)
                            )
                    ) {
                        Icon(
                            imageVector = if (isPlayingAdhan) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Preview Adhan",
                            tint = if (isPlayingAdhan) Color.Black else Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isNext) Color.White else Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}

/**
 * Polished 7-Day Weekly Schedule Card with highlighted today row.
 */
@Composable
fun WeeklyScheduleCard(
    location: UserLocation,
    method: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments,
    currentDate: Date
) {
    val todayCal = remember(currentDate) { Calendar.getInstance().apply { time = currentDate } }

    val days = remember(location, method, madhab, highLatRule, adjustments, currentDate) {
        val list = mutableListOf<PrayerTimes>()
        val cal = Calendar.getInstance().apply { time = currentDate }
        for (i in 0 until 7) {
            list.add(
                PrayerCalculationEngine.calculate(
                    date = cal.time,
                    location = location,
                    method = method,
                    madhab = madhab,
                    highLatRule = highLatRule,
                    adjustments = adjustments
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        list
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "7-Day Prayer Timetable",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Precise astronomical calculations for the coming week",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "7 Days",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Column Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Date", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(85.dp))
                Text(text = "Fajr", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(text = "Dhuhr", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(text = "Asr", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(text = "Maghrib", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(text = "Isha", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            days.forEachIndexed { idx, pt ->
                val isToday = idx == 0
                val rowContainer = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = rowContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 9.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(85.dp)
                        ) {
                            Text(
                                text = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(pt.date),
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(text = pt.formattedTime(PrayerName.FAJR), fontSize = 11.sp)
                        Text(text = pt.formattedTime(PrayerName.DHUHR), fontSize = 11.sp)
                        Text(text = pt.formattedTime(PrayerName.ASR), fontSize = 11.sp)
                        Text(text = pt.formattedTime(PrayerName.MAGHRIB), fontSize = 11.sp)
                        Text(text = pt.formattedTime(PrayerName.ISHA), fontSize = 11.sp)
                    }
                }
                if (idx < days.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                }
            }
        }
    }
}

/**
 * Polished Full-Month Calendar View.
 */
@Composable
fun MonthlyCalendarCard(
    location: UserLocation,
    method: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments,
    currentDate: Date
) {
    val cal = remember(currentDate) { Calendar.getInstance().apply { time = currentDate } }
    val monthName = remember(currentDate) { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentDate) }
    val currentDayOfMonth = remember(currentDate) { cal.get(Calendar.DAY_OF_MONTH) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = monthName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "Full Month",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Header labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Day", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(55.dp))
                Text(text = "Fajr", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(text = "Dhuhr", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(text = "Asr", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(text = "Maghrib", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                Text(text = "Isha", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            for (day in 1..daysInMonth) {
                cal.set(Calendar.DAY_OF_MONTH, day)
                val pt = PrayerCalculationEngine.calculate(
                    date = cal.time,
                    location = location,
                    method = method,
                    madhab = madhab,
                    highLatRule = highLatRule,
                    adjustments = adjustments
                )
                val isToday = day == currentDayOfMonth
                val rowBackground = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = rowBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${day}${getDaySuffix(day)}",
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(55.dp)
                        )
                        Text(text = pt.formattedTime(PrayerName.FAJR), fontSize = 11.sp)
                        Text(text = pt.formattedTime(PrayerName.DHUHR), fontSize = 11.sp)
                        Text(text = pt.formattedTime(PrayerName.ASR), fontSize = 11.sp)
                        Text(text = pt.formattedTime(PrayerName.MAGHRIB), fontSize = 11.sp)
                        Text(text = pt.formattedTime(PrayerName.ISHA), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun getDaySuffix(day: Int): String {
    if (day in 11..13) return "th"
    return when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

/**
 * Polished Astronomical Solstices & Equinoxes Preview.
 */
@Composable
fun YearAheadPreviewCard(
    location: UserLocation,
    method: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Astronomical Solar Milestones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Offline celestial Solstice & Equinox prayer timings",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "4 Seasons",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val milestones = listOf(
                Triple("Vernal Equinox", "March 21 • Equal Day & Night", Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.MARCH); set(Calendar.DAY_OF_MONTH, 21) }),
                Triple("Summer Solstice", "June 21 • Peak Solar Daylight", Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.JUNE); set(Calendar.DAY_OF_MONTH, 21) }),
                Triple("Autumnal Equinox", "September 22 • Equal Day & Night", Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.SEPTEMBER); set(Calendar.DAY_OF_MONTH, 22) }),
                Triple("Winter Solstice", "December 21 • Longest Night", Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.DECEMBER); set(Calendar.DAY_OF_MONTH, 21) })
            )

            milestones.forEachIndexed { idx, (title, subtitle, cal) ->
                val pt = PrayerCalculationEngine.calculate(
                    date = cal.time,
                    location = location,
                    method = method,
                    madhab = madhab,
                    highLatRule = highLatRule,
                    adjustments = adjustments
                )

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = subtitle,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PrayerTimeBadge("Fajr", pt.formattedTime(PrayerName.FAJR))
                            PrayerTimeBadge("Sunrise", pt.formattedTime(PrayerName.SUNRISE))
                            PrayerTimeBadge("Dhuhr", pt.formattedTime(PrayerName.DHUHR))
                            PrayerTimeBadge("Asr", pt.formattedTime(PrayerName.ASR))
                            PrayerTimeBadge("Maghrib", pt.formattedTime(PrayerName.MAGHRIB))
                            PrayerTimeBadge("Isha", pt.formattedTime(PrayerName.ISHA))
                        }
                    }
                }

                if (idx < milestones.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun PrayerTimeBadge(label: String, time: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = time,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
