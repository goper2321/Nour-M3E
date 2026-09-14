package com.example.features.prayer.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.NourDatabase
import com.example.database.PrayerLogEntity
import com.example.features.adhan.player.AdhanPlayer
import com.example.features.prayer.calculation.HijriDateHelper
import com.example.features.prayer.calculation.PrayerCalculationEngine
import com.example.features.prayer.models.CalculationMethod
import com.example.features.prayer.models.HighLatitudeRule
import com.example.features.prayer.models.Madhab
import com.example.features.prayer.models.PrayerAdjustments
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.PrayerTimes
import com.example.features.prayer.models.UserLocation
import com.example.features.widgets.CelestialPrayerHeroCard
import com.example.ui.m3e.M3EBadge
import com.example.ui.m3e.M3ECard
import com.example.ui.m3e.M3ECardHeader
import com.example.ui.m3e.M3ECardVariant
import com.example.ui.m3e.M3EPillTabRow
import com.example.ui.m3e.M3ESpacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * M3E Salah screen — same business logic, rebuilt presentation layer.
 *
 * Structure (m3e content-pane + card slots):
 * 1. Date header card (filled, header slot: Hijri EN/AR + Gregorian/zone)
 * 2. Salah obligation tracker (outlined, filter-chip row wired to Room)
 * 3. Celestial hero (elevated, countdown + arc + progress)
 * 4. Pill tab row (segmented-button port: Today / 7-Day / Monthly / Solstices)
 * 5. Tab content: prayer list / weekly table / monthly table / solar milestones
 */
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

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("prayer_screen_list"),
            contentPadding = PaddingValues(M3ESpacing.md),
            verticalArrangement = Arrangement.spacedBy(M3ESpacing.md)
        ) {
            // 1. Date header (m3e filled card, header slot)
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

                M3ECard(
                    variant = M3ECardVariant.Filled,
                    modifier = Modifier.testTag("prayer_header_card"),
                    header = {
                        M3ECardHeader(
                            eyebrow = "Hijri & Gregorian date",
                            title = hijriDate.formattedEn,
                            subtitle = "$gregorianDate • $currentZone",
                            trailing = {
                                Text(
                                    text = hijriDate.formattedAr,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                ) {
                    Text(
                        text = "${location.cityName} • ${calculationMethod.title.substringBefore(" (")} • ${madhab.title.substringBefore(" (")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Obligation tracker (m3e outlined card + filter chips, Room-backed)
            item {
                SalahTrackerCard(
                    logs = prayerLogs,
                    dateStr = todayStr,
                    onToggle = { prayer ->
                        coroutineScope.launch {
                            val existing = prayerLogs.firstOrNull { it.prayerName == prayer.name }
                            if (existing != null) {
                                db.prayerLogDao().deleteLog(existing.id)
                            } else {
                                db.prayerLogDao().insertOrUpdateLog(
                                    PrayerLogEntity(
                                        date = todayStr,
                                        prayerName = prayer.name,
                                        status = "PRAYED"
                                    )
                                )
                            }
                        }
                    }
                )
            }

            // 3. Celestial hero (m3e elevated card)
            item {
                CelestialPrayerHeroCard(
                    prayerTimes = todayPrayerTimes,
                    countdownState = countdownState,
                    now = currentTime
                )
            }

            // 4. Pill tabs (m3e segmented-button port)
            item {
                M3EPillTabRow(
                    tabs = listOf("Today", "7-Day", "Monthly", "Solstices"),
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it },
                    testTagPrefix = "prayer_tab"
                )
            }

            // 5. Tab content
            when (selectedTab) {
                0 -> {
                    // Today's Individual Prayer Cards
                    items(PrayerName.values()) { prayer ->
                        val isNext = countdownState.nextPrayer == prayer
                        val isCurrent = countdownState.currentPrayer == prayer
                        val isSunrise = prayer == PrayerName.SUNRISE
                        val isPlayingThis = playingAdhanPrayer == prayer && AdhanPlayer.isPlaying

                        PrayerCard(
                            prayer = prayer,
                            timeFormatted = todayPrayerTimes.formattedTime(prayer),
                            isNext = isNext,
                            isCurrent = isCurrent,
                            isSunrise = isSunrise,
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
                            }
                        )
                    }
                }
                1 -> {
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

/**
 * Salah obligation tracker — one-tap status pills persisted to Room.
 * Wires the previously collected-but-unused [logs] into the UI (UI-side only).
 */
@Composable
private fun SalahTrackerCard(
    logs: List<PrayerLogEntity>,
    dateStr: String,
    onToggle: (PrayerName) -> Unit,
    modifier: Modifier = Modifier
) {
    val tracked = listOf(
        PrayerName.FAJR, PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA
    )
    val prayed = logs.filter { it.status == "PRAYED" || it.status == "PRAYED_LATE" }
        .map { it.prayerName }.toSet()

    M3ECard(
        variant = M3ECardVariant.Outlined,
        modifier = modifier.testTag("salah_tracker_card"),
        header = {
            M3ECardHeader(
                eyebrow = "Daily tracker",
                title = "Today's Salah",
                subtitle = "${prayed.size} of 5 completed • $dateStr",
                trailing = { M3EBadge(text = "${prayed.size}/5") }
            )
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tracked.forEach { prayer ->
                val done = prayed.contains(prayer.name)
                FilterChip(
                    selected = done,
                    onClick = { onToggle(prayer) },
                    label = {
                        Text(
                            text = prayer.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (done) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = if (done) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tracker_${prayer.name}")
                )
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
    // Kept for API compatibility; new code uses M3EPillTabRow (segmented-button port).
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
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
 * Individual prayer row — m3e outlined card (elevated when NEXT).
 * Header slot: avatar + names + status badge. Trailing: preview + time.
 */
@Composable
fun PrayerCard(
    prayer: PrayerName,
    timeFormatted: String,
    isNext: Boolean,
    isCurrent: Boolean,
    isSunrise: Boolean,
    isPlayingAdhan: Boolean,
    onPlayAdhanToggle: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isNext -> MaterialTheme.colorScheme.primary
            isCurrent -> MaterialTheme.colorScheme.outline
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        },
        label = "card_border_color"
    )

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
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.surfaceContainer
        ),
        border = androidx.compose.foundation.BorderStroke(if (isNext || isCurrent) 2.dp else 1.dp, borderColor),
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
                // Avatar (m3e avatar pattern)
                Surface(
                    shape = CircleShape,
                    color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = prayerIcon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            M3EBadge(text = "NEXT", tonal = false)
                        } else if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            M3EBadge(text = "CURRENT")
                        } else if (isSunrise) {
                            Spacer(modifier = Modifier.width(8.dp))
                            M3EBadge(text = "SHURUQ")
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = prayer.arabicName + if (isSunrise) " • Shuruq" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (!isSunrise) {
                    IconButton(
                        onClick = onPlayAdhanToggle,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlayingAdhan) Icons.Default.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isPlayingAdhan) "Stop Adhan preview" else "Preview Adhan for ${prayer.displayName}",
                            tint = if (isPlayingAdhan) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * 7-Day timetable — m3e filled card with header/actions slots + list rows.
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

    M3ECard(
        variant = M3ECardVariant.Filled,
        header = {
            M3ECardHeader(
                eyebrow = "Timetable",
                title = "7-Day Prayer Timetable",
                subtitle = "Astronomical calculations for the coming week",
                trailing = { M3EBadge(text = "7 Days") }
            )
        }
    ) {
        // Column Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Date", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(85.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Fajr", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Dhuhr", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Asr", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Maghrib", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Isha", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        days.forEachIndexed { idx, pt ->
            val isToday = idx == 0
            val rowContainer = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else androidx.compose.ui.graphics.Color.Transparent

            Surface(
                shape = MaterialTheme.shapes.medium,
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
                    Text(
                        text = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(pt.date),
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.width(85.dp)
                    )
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

/**
 * Monthly timetable — m3e filled card, month pager in actions slot.
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
    var monthOffset by remember { mutableIntStateOf(0) }

    val cal = remember(currentDate, monthOffset) {
        Calendar.getInstance().apply {
            time = currentDate
            add(Calendar.MONTH, monthOffset)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    val monthName = remember(cal.time) { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time) }

    val todayCal = remember(currentDate) { Calendar.getInstance().apply { time = currentDate } }
    val isCurrentMonth = todayCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR) &&
            todayCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH)
    val todayDay = if (isCurrentMonth) todayCal.get(Calendar.DAY_OF_MONTH) else -1

    M3ECard(
        variant = M3ECardVariant.Filled,
        header = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MONTHLY TIMETABLE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { monthOffset-- },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous month",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { monthOffset++ },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next month",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Date", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(65.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Fajr", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Dhuhr", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Asr", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Maghrib", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Isha", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayCal = cal.clone() as Calendar

        for (day in 1..daysInMonth) {
            dayCal.set(Calendar.DAY_OF_MONTH, day)
            val pt = PrayerCalculationEngine.calculate(
                date = dayCal.time,
                location = location,
                method = method,
                madhab = madhab,
                highLatRule = highLatRule,
                adjustments = adjustments
            )
            val isToday = day == todayDay
            val rowBackground = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent
            val textColor = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

            Surface(
                shape = MaterialTheme.shapes.small,
                color = rowBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("EEE d", Locale.getDefault()).format(dayCal.time),
                        fontSize = 11.sp,
                        fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.width(65.dp)
                    )
                    Text(text = pt.formattedTime(PrayerName.FAJR), fontSize = 11.sp, color = textColor)
                    Text(text = pt.formattedTime(PrayerName.DHUHR), fontSize = 11.sp, color = textColor)
                    Text(text = pt.formattedTime(PrayerName.ASR), fontSize = 11.sp, color = textColor)
                    Text(text = pt.formattedTime(PrayerName.MAGHRIB), fontSize = 11.sp, color = textColor)
                    Text(text = pt.formattedTime(PrayerName.ISHA), fontSize = 11.sp, color = textColor)
                }
            }
        }
    }
}

/**
 * Solar milestones — m3e filled card + nested outlined milestone cards.
 */
@Composable
fun YearAheadPreviewCard(
    location: UserLocation,
    method: CalculationMethod,
    madhab: Madhab,
    highLatRule: HighLatitudeRule,
    adjustments: PrayerAdjustments
) {
    M3ECard(
        variant = M3ECardVariant.Filled,
        header = {
            M3ECardHeader(
                eyebrow = "Astronomy",
                title = "Solar Milestones",
                subtitle = "Offline Solstice & Equinox timings",
                trailing = { M3EBadge(text = "4 Seasons") }
            )
        }
    ) {
        val milestones = listOf(
            Triple("Vernal Equinox", "March 21 • Equal Day & Night", Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.MARCH); set(Calendar.DAY_OF_MONTH, 21) }),
            Triple("Summer Solstice", "June 21 • Peak Daylight", Calendar.getInstance().apply { set(Calendar.MONTH, Calendar.JUNE); set(Calendar.DAY_OF_MONTH, 21) }),
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

            M3ECard(
                variant = M3ECardVariant.Outlined,
                header = {
                    Column {
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
                }
            ) {
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

            if (idx < milestones.size - 1) {
                Spacer(modifier = Modifier.height(2.dp))
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
