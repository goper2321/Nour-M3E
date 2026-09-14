package com.example.features.qibla.ui

import android.hardware.SensorManager
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.prayer.models.UserLocation
import com.example.features.qibla.QiblaSensorManager
import com.example.ui.m3e.M3EAlertDialog
import com.example.ui.m3e.M3EButton
import com.example.ui.m3e.M3EButtonSize
import com.example.ui.m3e.M3EButtonVariant
import com.example.ui.m3e.M3ECard
import com.example.ui.m3e.M3ECardHeader
import com.example.ui.m3e.M3ECardVariant
import com.example.ui.m3e.M3EMetric
import com.example.ui.m3e.M3ESliderSection
import com.example.ui.m3e.M3ESpacing
import com.example.ui.m3e.M3EStatusChip
import kotlin.math.cos
import kotlin.math.sin

/**
 * M3E Qibla screen — sensor math preserved, presentation rebuilt.
 *
 * Structure (m3e cards):
 * 1. Location + accuracy header (filled card, status-chip trailing)
 * 2. Aligned banner (tonal, animated)
 * 3. Compass dial (elevated card wrapping the canvas + hub)
 * 4. Bearing + distance metrics (filled card)
 * 5. Solar cross-check (outlined card)
 * 6. Manual fallback (outlined card + slider section)
 * 7. Calibration dialog (m3e dialog port)
 */
@Composable
fun QiblaScreen(
    location: UserLocation,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sensorManager = remember { QiblaSensorManager(context) }

    DisposableEffect(location) {
        sensorManager.setLocation(location.latitude, location.longitude)
        sensorManager.startListening()
        onDispose {
            sensorManager.stopListening()
        }
    }

    val qiblaState by sensorManager.qiblaState.collectAsState()
    var showCalibrationDialog by remember { mutableStateOf(false) }
    var manualMode by remember { mutableStateOf(!qiblaState.hasMagnetometer) }
    var manualHeading by remember { mutableFloatStateOf(0f) }

    val effectiveHeading = if (manualMode) manualHeading else qiblaState.deviceHeading
    val relativeAngle = (qiblaState.qiblaBearing - effectiveHeading + 360f) % 360f
    val isFacingQibla = relativeAngle in 357f..360f || relativeAngle in 0f..3f

    val animatedRotation by animateFloatAsState(
        targetValue = -effectiveHeading,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        label = "compass_rotation"
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(M3ESpacing.md),
            verticalArrangement = Arrangement.spacedBy(M3ESpacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Location + accuracy header
            item {
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
                                    text = "QIBLA DIRECTION",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Qibla Direction",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${location.cityName} → Kaaba, Makkah",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (accuracyText, accuracyColor) = when (qiblaState.accuracy) {
                                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Accurate" to MaterialTheme.colorScheme.primary
                                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium" to MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> "Calibrate" to MaterialTheme.colorScheme.error
                                }
                                M3EStatusChip(
                                    text = accuracyText,
                                    color = accuracyColor,
                                    modifier = Modifier.testTag("qibla_accuracy_indicator")
                                )
                                IconButton(onClick = { showCalibrationDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Calibration guide",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                ) {
                    Text(
                        text = "Bearing ${String.format(Locale.getDefault(), "%.1f°", qiblaState.qiblaBearing)} from true north • ${String.format(Locale.getDefault(), "%,.0f km", qiblaState.distanceKm)} to Makkah",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 2. Aligned banner
            item {
                AnimatedVisibility(visible = isFacingQibla) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("facing_qibla_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Aligned with Kaaba"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Allahu Akbar — You are facing the Holy Kaaba!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // 3. Compass dial (elevated card)
            item {
                M3ECard(
                    variant = M3ECardVariant.Elevated,
                    header = {
                        M3ECardHeader(
                            eyebrow = "Live compass",
                            title = "Compass Rose",
                            subtitle = if (manualMode) "Manual bearing ${manualHeading.toInt()}°" else "Heading ${effectiveHeading.toInt()}° • ${relativeAngle.toInt()}° to Qibla"
                        )
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("qibla_compass_dial"),
                        contentAlignment = Alignment.Center
                    ) {
                        QiblaDial(
                            animatedRotation = animatedRotation,
                            qiblaBearing = qiblaState.qiblaBearing,
                            sunAzimuth = qiblaState.sunAzimuth,
                            isFacingQibla = isFacingQibla
                        )
                    }
                }
            }

            // 4. Metrics
            item {
                M3ECard(
                    variant = M3ECardVariant.Filled,
                    header = {
                        M3ECardHeader(
                            eyebrow = "Great-circle math",
                            title = "Bearing & Distance",
                            subtitle = "On-device spherical trigonometry"
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        M3EMetric(
                            label = "Qibla Bearing",
                            value = String.format(Locale.getDefault(), "%.1f°", qiblaState.qiblaBearing),
                            caption = "From True North"
                        )
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        M3EMetric(
                            label = "Distance to Kaaba",
                            value = String.format(Locale.getDefault(), "%,.0f km", qiblaState.distanceKm),
                            caption = "Makkah Al-Mukarramah",
                            valueColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            // 5. Solar cross-check
            item {
                M3ECard(
                    variant = M3ECardVariant.Outlined,
                    header = {
                        M3ECardHeader(
                            eyebrow = "Verification",
                            title = "Sun-Based Cross-Check",
                            subtitle = "Current sun bearing ≈ ${String.format(Locale.getDefault(), "%.0f°", qiblaState.sunAzimuth)}"
                        )
                    }
                ) {
                    Text(
                        text = "Look at the sun's physical position outside to confirm device compass orientation. The amber dot on the dial marks the solar azimuth.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 6. Manual fallback
            item {
                M3ECard(
                    variant = M3ECardVariant.Outlined,
                    header = {
                        M3ECardHeader(
                            title = "Manual Bearing Fallback",
                            subtitle = if (manualMode) "Sensors bypassed" else "Using live sensors",
                            trailing = {
                                M3EButton(
                                    onClick = { manualMode = !manualMode },
                                    variant = M3EButtonVariant.Outlined,
                                    size = M3EButtonSize.ExtraSmall,
                                    modifier = Modifier.testTag("toggle_manual_bearing")
                                ) {
                                    Text(if (manualMode) "Use Sensors" else "Manual Slider", fontSize = 11.sp)
                                }
                            }
                        )
                    }
                ) {
                    if (manualMode) {
                        M3ESliderSection(
                            title = "Phone orientation",
                            valueLabel = "${manualHeading.toInt()}°"
                        ) {
                            Slider(
                                value = manualHeading,
                                onValueChange = {
                                    manualHeading = it
                                    sensorManager.setManualHeading(it)
                                },
                                valueRange = 0f..360f,
                                modifier = Modifier.testTag("manual_bearing_slider")
                            )
                        }
                    } else {
                        Text(
                            text = "No magnetometer? Enable manual mode to rotate orientation with the slider.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // 7. Calibration dialog (m3e dialog port)
    if (showCalibrationDialog) {
        M3EAlertDialog(
            onDismiss = { showCalibrationDialog = false },
            title = "Sensor Calibration Guidance",
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "To ensure high compass accuracy for the Qibla, your phone's magnetometer must be calibrated against magnetic interference.")
                    M3ECard(variant = M3ECardVariant.Filled) {
                        Column {
                            Text(
                                text = "Figure-8 motion:",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Hold your phone flat and wave it gently in a continuous figure-8 (∞) motion 3 to 5 times.",
                                fontSize = 13.sp
                            )
                        }
                    }
                    Text(
                        text = "Keep away from large metal objects, speakers, laptop magnets, or thick magnetic phone cases. Keep the phone parallel to the ground.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    )
                }
            },
            confirm = {
                M3EButton(onClick = { showCalibrationDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

/** Compass rose canvas + hub extracted for readability (math unchanged). */
@Composable
private fun QiblaDial(
    animatedRotation: Float,
    qiblaBearing: Float,
    sunAzimuth: Float,
    isFacingQibla: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(300.dp),
        contentAlignment = Alignment.Center
    ) {
        val dialBorderColor = MaterialTheme.colorScheme.outlineVariant
        val tickColor = MaterialTheme.colorScheme.outline
        val northColor = MaterialTheme.colorScheme.primary
        val southColor = MaterialTheme.colorScheme.outline
        val needleColor = MaterialTheme.colorScheme.primary
        val pointerOuter = MaterialTheme.colorScheme.primary
        val pointerInner = MaterialTheme.colorScheme.surfaceContainerLow
        val sunIndicatorColor = MaterialTheme.colorScheme.tertiary
        val hubColor = if (isFacingQibla) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        val hubTextColor = if (isFacingQibla) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

        Canvas(
            modifier = Modifier
                .size(280.dp)
                .rotate(animatedRotation)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            drawCircle(color = dialBorderColor, radius = radius, style = Stroke(width = 3.dp.toPx()))

            for (deg in 0 until 360 step 10) {
                val isMajor = deg % 30 == 0
                val tickLen = if (isMajor) 18.dp.toPx() else 10.dp.toPx()
                val rad = Math.toRadians(deg.toDouble())
                val outerX = center.x + (radius - 8.dp.toPx()) * sin(rad).toFloat()
                val outerY = center.y - (radius - 8.dp.toPx()) * cos(rad).toFloat()
                val innerX = center.x + (radius - 8.dp.toPx() - tickLen) * sin(rad).toFloat()
                val innerY = center.y - (radius - 8.dp.toPx() - tickLen) * cos(rad).toFloat()

                drawLine(
                    color = if (deg == 0) northColor else tickColor,
                    start = Offset(innerX, innerY),
                    end = Offset(outerX, outerY),
                    strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()
                )
            }

            val northPath = Path().apply {
                moveTo(center.x, center.y - radius + 30.dp.toPx())
                lineTo(center.x - 10.dp.toPx(), center.y - 40.dp.toPx())
                lineTo(center.x + 10.dp.toPx(), center.y - 40.dp.toPx())
                close()
            }
            drawPath(path = northPath, color = northColor)

            val southPath = Path().apply {
                moveTo(center.x, center.y + radius - 30.dp.toPx())
                lineTo(center.x - 8.dp.toPx(), center.y + 40.dp.toPx())
                lineTo(center.x + 8.dp.toPx(), center.y + 40.dp.toPx())
                close()
            }
            drawPath(path = southPath, color = southColor)

            val qiblaRad = Math.toRadians(qiblaBearing.toDouble())
            val qiblaX = center.x + (radius - 45.dp.toPx()) * sin(qiblaRad).toFloat()
            val qiblaY = center.y - (radius - 45.dp.toPx()) * cos(qiblaRad).toFloat()

            val needlePath = Path().apply {
                moveTo(qiblaX, qiblaY)
                val baseLeftX = center.x + 14.dp.toPx() * sin(qiblaRad - Math.PI / 2).toFloat()
                val baseLeftY = center.y - 14.dp.toPx() * cos(qiblaRad - Math.PI / 2).toFloat()
                val baseRightX = center.x + 14.dp.toPx() * sin(qiblaRad + Math.PI / 2).toFloat()
                val baseRightY = center.y - 14.dp.toPx() * cos(qiblaRad + Math.PI / 2).toFloat()
                lineTo(baseLeftX, baseLeftY)
                lineTo(baseRightX, baseRightY)
                close()
            }
            drawPath(needlePath, color = needleColor)

            drawCircle(color = pointerOuter, radius = 9.dp.toPx(), center = Offset(qiblaX, qiblaY))
            drawCircle(color = pointerInner, radius = 4.dp.toPx(), center = Offset(qiblaX, qiblaY))

            val sunRad = Math.toRadians(sunAzimuth.toDouble())
            val sunX = center.x + (radius - 20.dp.toPx()) * sin(sunRad).toFloat()
            val sunY = center.y - (radius - 20.dp.toPx()) * cos(sunRad).toFloat()
            drawCircle(color = sunIndicatorColor, radius = 6.dp.toPx(), center = Offset(sunX, sunY))
        }

        Surface(
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = hubColor,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🕋", fontSize = 24.sp)
                Text(
                    text = "QIBLA",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = hubTextColor
                )
            }
        }
    }
}
