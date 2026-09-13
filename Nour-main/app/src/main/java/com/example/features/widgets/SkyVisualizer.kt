package com.example.features.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.features.prayer.models.CountdownState
import com.example.features.prayer.models.PrayerName
import com.example.features.prayer.models.PrayerTimes
import com.example.ui.theme.MonoPlatinum
import java.util.Date
import java.util.Locale

/**
 * Computes dynamic atmospheric gradient matching live solar altitude.
 */
fun getSkyGradient(prayerTimes: PrayerTimes, nowMillis: Long): List<Color> {
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time
    val fajrMillis = prayerTimes.fajr.time
    val dhuhrMillis = prayerTimes.dhuhr.time
    val asrMillis = prayerTimes.asr.time
    val ishaMillis = prayerTimes.isha.time

    return when {
        nowMillis < fajrMillis -> listOf(
            Color(0xFF000000),
            Color(0xFF09090B),
            Color(0xFF141416),
            Color(0xFF1F1F24)
        )
        nowMillis in fajrMillis until sunriseMillis -> listOf(
            Color(0xFF09090B),
            Color(0xFF18181D),
            Color(0xFF27272F),
            Color(0xFF3F3F4A)
        )
        nowMillis in sunriseMillis until dhuhrMillis -> listOf(
            Color(0xFF141418),
            Color(0xFF25252D),
            Color(0xFF3C3C47),
            Color(0xFF555562)
        )
        nowMillis in dhuhrMillis until asrMillis -> listOf(
            Color(0xFF1C1C22),
            Color(0xFF2E2E37),
            Color(0xFF454552),
            Color(0xFF626270)
        )
        nowMillis in asrMillis until sunsetMillis -> listOf(
            Color(0xFF141418),
            Color(0xFF23232B),
            Color(0xFF373742),
            Color(0xFF4C4C58)
        )
        nowMillis in sunsetMillis until ishaMillis -> listOf(
            Color(0xFF0A0A0D),
            Color(0xFF16161D),
            Color(0xFF2A2A35),
            Color(0xFF3E3E4C)
        )
        else -> listOf(
            Color(0xFF000000),
            Color(0xFF08080A),
            Color(0xFF121215),
            Color(0xFF1A1A1E)
        )
    }
}

/**
 * Returns current celestial stage title and descriptive caption.
 */
fun getCelestialStage(prayerTimes: PrayerTimes, nowMillis: Long): Pair<String, String> {
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time
    val fajrMillis = prayerTimes.fajr.time
    val dhuhrMillis = prayerTimes.dhuhr.time
    val asrMillis = prayerTimes.asr.time
    val ishaMillis = prayerTimes.isha.time

    return when {
        nowMillis < fajrMillis -> "Night Sky • Tahajjud Window" to "Deep astronomical night until Fajr"
        nowMillis in fajrMillis until sunriseMillis -> "Dawn Twilight • Fajr" to "First light of dawn appearing"
        nowMillis in sunriseMillis until dhuhrMillis -> "Morning Sun • Shuruq" to "Sun rising towards celestial zenith"
        nowMillis in dhuhrMillis until asrMillis -> "Solar Zenith • High Noon" to "Sun at maximum daily elevation"
        nowMillis in asrMillis until sunsetMillis -> "Afternoon • Asr" to "Sun descending with gentle silver light"
        nowMillis in sunsetMillis until ishaMillis -> "Dusk Twilight • Maghrib" to "Sun dipping below the horizon"
        else -> "Night Sky • Isha" to "Night settled with celestial stars"
    }
}

/**
 * Full-page living immersive sky background for the "Salah" page.
 * Features:
 * - Real-time atmospheric color gradients matching the sun/moon phase.
 * - Ambient twinkling star constellations (visible at night, dawn, and dusk).
 * - Soft atmospheric horizon aura and celestial haze.
 * - Subtle bottom vignette to ensure smooth list readability.
 */
@Composable
fun ImmersiveSkyBackground(
    prayerTimes: PrayerTimes,
    now: Date = Date(),
    modifier: Modifier = Modifier
) {
    val nowMillis = now.time
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time
    val isDaytime = nowMillis in sunriseMillis..sunsetMillis

    val skyGradient = remember(nowMillis, prayerTimes) {
        getSkyGradient(prayerTimes, nowMillis)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "immersive_sky_anim")
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sky_star_twinkle"
    )
    val horizonBreath by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "horizon_breath"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(skyGradient))
    ) {
        // Celestial Canvas for ambient stars, atmospheric haze, and subtle celestial light
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Stars (active during night, dawn twilight, or dusk twilight)
            val showStars = nowMillis < sunriseMillis || nowMillis > sunsetMillis
            if (showStars) {
                val starPositions = listOf(
                    Offset(w * 0.08f, h * 0.05f),
                    Offset(w * 0.22f, h * 0.09f),
                    Offset(w * 0.38f, h * 0.03f),
                    Offset(w * 0.55f, h * 0.07f),
                    Offset(w * 0.72f, h * 0.04f),
                    Offset(w * 0.88f, h * 0.08f),
                    Offset(w * 0.15f, h * 0.16f),
                    Offset(w * 0.31f, h * 0.21f),
                    Offset(w * 0.65f, h * 0.18f),
                    Offset(w * 0.82f, h * 0.24f),
                    Offset(w * 0.93f, h * 0.15f),
                    Offset(w * 0.10f, h * 0.32f),
                    Offset(w * 0.48f, h * 0.29f),
                    Offset(w * 0.76f, h * 0.35f),
                    Offset(w * 0.25f, h * 0.44f),
                    Offset(w * 0.85f, h * 0.48f),
                    Offset(w * 0.05f, h * 0.58f),
                    Offset(w * 0.60f, h * 0.62f),
                    Offset(w * 0.90f, h * 0.70f),
                    Offset(w * 0.40f, h * 0.78f)
                )
                starPositions.forEachIndexed { index, pos ->
                    val factor = if (index % 3 == 0) starTwinkle else (1.15f - starTwinkle).coerceIn(0.2f, 1f)
                    val radius = if (index % 2 == 0) 1.8.dp.toPx() else 1.2.dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = factor * if (isDaytime) 0f else 0.80f),
                        radius = radius,
                        center = pos
                    )
                }
            }

            // 2. Soft Horizon Aura Glow (mimicking atmospheric lunar / solar silver scattering)
            val horizonY = h * 0.35f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        (if (isDaytime) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.10f)),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, horizonY),
                    radius = w * 0.85f * horizonBreath
                ),
                radius = w * 0.85f * horizonBreath,
                center = Offset(w * 0.5f, horizonY)
            )

            // 3. Faint bottom vignette for smooth scroll readability
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.25f),
                        Color.Black.copy(alpha = 0.55f)
                    ),
                    startY = h * 0.45f,
                    endY = h
                ),
                size = Size(w, h * 0.55f),
                topLeft = Offset(0f, h * 0.45f)
            )
        }
    }
}

/**
 * Modern Weather-App Inspired Sun & Moon Cycle & Salah Countdown Card.
 * Floating as an elegant frosted glass module over the immersive sky background.
 * Featuring:
 * - Frosted glassmorphism that harmonizes with the living background.
 * - Weather-app style category eyebrow ("SUN & MOON CYCLE") with fancy vector icon.
 * - Continuous 24h sinusoidal celestial wave with day dome, night dome, and horizon line.
 * - Animated fancy SVGs for Sun and Moon (rotating rays, lunar glow, and star clusters).
 * - Live sun altitude / moon progress tracking along the celestial arc.
 * - Weather app style countdown capsules and time-to-event callout.
 * - 4-tile weather celestial metrics row (Sunrise, Solar Zenith, Sunset, Nightfall) with custom SVG icons.
 */
@Composable
fun CelestialPrayerHeroCard(
    prayerTimes: PrayerTimes,
    countdownState: CountdownState,
    now: Date = Date(),
    modifier: Modifier = Modifier
) {
    val nowMillis = now.time
    val sunriseMillis = prayerTimes.sunrise.time
    val sunsetMillis = prayerTimes.maghrib.time
    val fajrMillis = prayerTimes.fajr.time
    val dhuhrMillis = prayerTimes.dhuhr.time
    val asrMillis = prayerTimes.asr.time
    val ishaMillis = prayerTimes.isha.time

    val isDaytime = nowMillis in sunriseMillis..sunsetMillis

    // Daytime progress (0f at sunrise, 1f at sunset)
    val daytimeProgress = if (isDaytime) {
        val totalDay = (sunsetMillis - sunriseMillis).coerceAtLeast(1L)
        ((nowMillis - sunriseMillis).toDouble() / totalDay).toFloat().coerceIn(0f, 1f)
    } else {
        0.5f
    }

    // Nighttime progress (0f at sunset, 1f at sunrise)
    val nighttimeProgress = if (!isDaytime) {
        if (nowMillis >= sunsetMillis) {
            val tomorrowSunrise = sunriseMillis + (24 * 3600 * 1000L)
            val totalNight = (tomorrowSunrise - sunsetMillis).coerceAtLeast(1L)
            ((nowMillis - sunsetMillis).toDouble() / totalNight).toFloat().coerceIn(0f, 1f)
        } else {
            val yesterdaySunset = sunsetMillis - (24 * 3600 * 1000L)
            val totalNight = (sunriseMillis - yesterdaySunset).coerceAtLeast(1L)
            ((nowMillis - yesterdaySunset).toDouble() / totalNight).toFloat().coerceIn(0f, 1f)
        }
    } else {
        0.5f
    }

    // Celestial state title & description
    val celestialStage = remember(prayerTimes, nowMillis) {
        getCelestialStage(prayerTimes, nowMillis)
    }

    // Infinite animation for subtle celestial sun ray rotation and atmospheric twinkle
    val infiniteTransition = rememberInfiniteTransition(label = "celestial_effects")
    val sunRayRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_ray_rotation"
    )
    val starTwinkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_twinkle"
    )

    // Countdown hours, minutes, seconds
    val totalSeconds = (countdownState.remainingMillis / 1000).coerceAtLeast(0L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val hoursFormatted = String.format(Locale.US, "%02d", hours)
    val minutesFormatted = String.format(Locale.US, "%02d", minutes)
    val secondsFormatted = String.format(Locale.US, "%02d", seconds)

    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("celestial_prayer_card"),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.28f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            // 1. Weather App Style Header: Category Eyebrow + Live Celestial Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Eyebrow with Sun/Moon Cycle SVG
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_sun_moon_cycle),
                                contentDescription = "Sun and Moon Cycle",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "SUN & MOON CYCLE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = celestialStage.first,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                // Weather-style Event Badge (e.g. Daylight % or Night %)
                Surface(
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.18f))
                ) {
                    Text(
                        text = if (isDaytime) "Daylight ${(daytimeProgress * 100).toInt()}%" else "Night ${(nighttimeProgress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.95f),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Weather-App Style Primary Countdown Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Next: ${countdownState.nextPrayer.displayName}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = countdownState.nextPrayer.arabicName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Starts at ${prayerTimes.formattedTime(countdownState.nextPrayer)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                // Segmented Weather-Style Countdown Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    WeatherCountdownCapsule(value = hoursFormatted, unit = "HR")
                    Text(
                        text = ":",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    WeatherCountdownCapsule(value = minutesFormatted, unit = "MIN")
                    Text(
                        text = ":",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    WeatherCountdownCapsule(value = secondsFormatted, unit = "SEC")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Signature Weather App Sun & Moon Celestial Trajectory (Canvas + Fancy SVGs)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Weather-style Celestial Arc Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCelestialWaveAndSky(
                        isDaytime = isDaytime,
                        dayProgress = daytimeProgress,
                        nightProgress = nighttimeProgress,
                        starTwinkle = starTwinkleAlpha
                    )
                }

                // Render Fancy Animated Sun SVG or Fancy Moon SVG at the computed position
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isDaytime) {
                        // Sun Position along daytime parabolic arc
                        val t = daytimeProgress
                        val xFraction = 0.08f + (0.84f * t)
                        val yFraction = 0.78f - (0.64f * 4 * t * (1 - t))

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(
                                    start = (xFraction * 280).dp,
                                    top = (yFraction * 75).dp
                                )
                                .size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Radiant rotating Sun SVG
                            Image(
                                painter = painterResource(id = R.drawable.ic_fancy_sun),
                                contentDescription = "Daylight Sun",
                                modifier = Modifier
                                    .size(38.dp)
                                    .rotate(sunRayRotation)
                            )
                        }
                    } else {
                        // Moon Position along nocturnal wave arc
                        val t = nighttimeProgress
                        val xFraction = 0.08f + (0.84f * t)
                        val yFraction = 0.78f - (0.60f * 4 * t * (1 - t))

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(
                                    start = (xFraction * 280).dp,
                                    top = (yFraction * 75).dp
                                )
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Fancy Crescent Moon with twinkling stars
                            Image(
                                painter = painterResource(id = R.drawable.ic_fancy_crescent_moon),
                                contentDescription = "Nighttime Crescent Moon",
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                // Milestone Labels along the celestial horizon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CelestialMilestoneLabel(
                        name = "Dawn",
                        time = prayerTimes.formattedTime(PrayerName.FAJR),
                        isActive = nowMillis in fajrMillis until sunriseMillis
                    )
                    CelestialMilestoneLabel(
                        name = "Sunrise",
                        time = prayerTimes.formattedTime(PrayerName.SUNRISE),
                        isActive = nowMillis in sunriseMillis until dhuhrMillis
                    )
                    CelestialMilestoneLabel(
                        name = "Zenith",
                        time = prayerTimes.formattedTime(PrayerName.DHUHR),
                        isActive = nowMillis in dhuhrMillis until asrMillis
                    )
                    CelestialMilestoneLabel(
                        name = "Sunset",
                        time = prayerTimes.formattedTime(PrayerName.MAGHRIB),
                        isActive = nowMillis in sunsetMillis until ishaMillis
                    )
                    CelestialMilestoneLabel(
                        name = "Dusk",
                        time = prayerTimes.formattedTime(PrayerName.ISHA),
                        isActive = nowMillis >= ishaMillis || nowMillis < fajrMillis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Weather App Progress Strip towards next Salah
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${countdownState.currentPrayer.displayName} → ${countdownState.nextPrayer.displayName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${(countdownState.progress * 100).toInt()}% elapsed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { countdownState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.20f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Four-Tile Modern Weather Metrics Strip (Sunrise, Zenith, Sunset, Nightfall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeatherMetricTile(
                    iconRes = R.drawable.ic_weather_sunrise,
                    label = "Sunrise",
                    time = prayerTimes.formattedTime(PrayerName.SUNRISE),
                    isPassed = nowMillis > sunriseMillis,
                    modifier = Modifier.weight(1f)
                )
                WeatherMetricTile(
                    iconRes = R.drawable.ic_weather_solar_noon,
                    label = "Solar Noon",
                    time = prayerTimes.formattedTime(PrayerName.DHUHR),
                    isPassed = nowMillis > dhuhrMillis,
                    modifier = Modifier.weight(1f)
                )
                WeatherMetricTile(
                    iconRes = R.drawable.ic_weather_sunset,
                    label = "Sunset",
                    time = prayerTimes.formattedTime(PrayerName.MAGHRIB),
                    isPassed = nowMillis > sunsetMillis,
                    modifier = Modifier.weight(1f)
                )
                WeatherMetricTile(
                    iconRes = R.drawable.ic_weather_twilight,
                    label = "Nightfall",
                    time = prayerTimes.formattedTime(PrayerName.ISHA),
                    isPassed = nowMillis > ishaMillis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Subtle Expandable Daylight / Night Summary Pill
            Surface(
                onClick = { showDetails = !showDetails },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.24f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val daylightDurationMillis = (sunsetMillis - sunriseMillis).coerceAtLeast(0L)
                        val dlHours = daylightDurationMillis / (3600 * 1000)
                        val dlMinutes = (daylightDurationMillis % (3600 * 1000)) / (60 * 1000)

                        Text(
                            text = "Daylight: ${dlHours}h ${dlMinutes}m • ${if (isDaytime) "Sun above horizon" else "Night cycle active"}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = if (showDetails) "Hide details ▲" else "Details ▼",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AnimatedVisibility(
                        visible = showDetails,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = celestialStage.second,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Adhan notifications & Qibla direction are synchronized with this exact solar trajectory.",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws the 24-hour celestial wave path, horizon line, glow fill, and twinkling stars.
 */
private fun DrawScope.drawCelestialWaveAndSky(
    isDaytime: Boolean,
    dayProgress: Float,
    nightProgress: Float,
    starTwinkle: Float
) {
    val w = size.width
    val h = size.height
    val horizonY = h * 0.72f
    val waveHeight = h * 0.52f

    // 1. Draw Star Constellations in the sky
    if (!isDaytime) {
        val stars = listOf(
            Offset(w * 0.08f, h * 0.18f),
            Offset(w * 0.18f, h * 0.28f),
            Offset(w * 0.32f, h * 0.12f),
            Offset(w * 0.45f, h * 0.34f),
            Offset(w * 0.60f, h * 0.16f),
            Offset(w * 0.74f, h * 0.24f),
            Offset(w * 0.86f, h * 0.14f),
            Offset(w * 0.94f, h * 0.32f)
        )
        stars.forEachIndexed { i, offset ->
            val radius = if (i % 2 == 0) 1.6.dp.toPx() else 2.2.dp.toPx()
            val alpha = if (i % 2 == 0) starTwinkle else (1.2f - starTwinkle).coerceIn(0.3f, 1f)
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = radius,
                center = offset
            )
        }
    }

    // 2. Daytime Celestial Arc
    val dayArc = Path().apply {
        moveTo(w * 0.05f, horizonY)
        quadraticTo(w * 0.50f, horizonY - waveHeight, w * 0.95f, horizonY)
    }

    // Gradient fill under the daytime sun curve
    val fillPath = Path().apply {
        moveTo(w * 0.05f, horizonY)
        quadraticTo(w * 0.50f, horizonY - waveHeight, w * 0.95f, horizonY)
        lineTo(w * 0.95f, horizonY)
        lineTo(w * 0.05f, horizonY)
        close()
    }
    drawPath(
        path = fillPath,
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.04f),
                Color.Transparent
            ),
            startY = horizonY - waveHeight,
            endY = horizonY
        )
    )

    // Dotted celestial trajectory path
    drawPath(
        path = dayArc,
        color = Color.White.copy(alpha = 0.35f),
        style = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    )

    // 3. Horizon Reference Line (Weather app style)
    drawLine(
        color = Color.White.copy(alpha = 0.25f),
        start = Offset(0f, horizonY),
        end = Offset(w, horizonY),
        strokeWidth = 1.2.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
    )

    // Current position indicator line down to horizon
    val activeT = if (isDaytime) dayProgress else nightProgress
    val currentX = w * (0.05f + 0.90f * activeT)
    val currentY = horizonY - (4 * waveHeight * activeT * (1 - activeT))

    // Vertical dashed marker beam
    drawLine(
        color = Color.White.copy(alpha = 0.40f),
        start = Offset(currentX, currentY),
        end = Offset(currentX, horizonY),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
    )

    // Small glowing ground marker on the horizon
    drawCircle(
        color = Color.White,
        radius = 3.dp.toPx(),
        center = Offset(currentX, horizonY)
    )
}

/**
 * Weather app style capsule for hours, minutes, and seconds.
 */
@Composable
private fun WeatherCountdownCapsule(
    value: String,
    unit: String
) {
    Surface(
        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.35f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = Color.White
            )
            Text(
                text = unit,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = MonoPlatinum,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Text label for milestones along the horizon (Dawn, Sunrise, Zenith, Sunset, Dusk).
 */
@Composable
private fun CelestialMilestoneLabel(
    name: String,
    time: String,
    isActive: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = name,
            fontSize = 9.sp,
            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.65f)
        )
        Text(
            text = time,
            fontSize = 8.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.50f)
        )
    }
}

/**
 * Clean Weather Metric Tile (Sunrise, Solar Noon, Sunset, Nightfall) with vector SVG.
 */
@Composable
private fun WeatherMetricTile(
    iconRes: Int,
    label: String,
    time: String,
    isPassed: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.scrim.copy(alpha = if (isPassed) 0.28f else 0.40f),
        border = BorderStroke(
            1.dp,
            if (isPassed) Color.White.copy(alpha = 0.10f) else Color.White.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.70f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = time,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPassed) Color.White.copy(alpha = 0.75f) else Color.White,
                maxLines = 1
            )
        }
    }
}
