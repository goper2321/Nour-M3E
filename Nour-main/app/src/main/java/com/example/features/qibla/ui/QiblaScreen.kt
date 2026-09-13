package com.example.features.qibla.ui

import android.hardware.SensorManager
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.prayer.models.UserLocation
import com.example.features.qibla.QiblaData
import com.example.features.qibla.QiblaSensorManager
import kotlin.math.cos
import kotlin.math.sin

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

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Location and Accuracy Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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

                // Sensor Accuracy Chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (accuracyText, accuracyColor) = when (qiblaState.accuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "Accurate" to MaterialTheme.colorScheme.primary
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium" to MaterialTheme.colorScheme.onSurfaceVariant
                        else -> "Calibrate" to MaterialTheme.colorScheme.outline
                    }

                    Surface(
                        color = accuracyColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.testTag("qibla_accuracy_indicator")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(accuracyColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = accuracyText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = accuracyColor
                            )
                        }
                    }

                    IconButton(onClick = { showCalibrationDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Calibration Guide",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Facing Qibla Banner
        AnimatedVisibility(visible = isFacingQibla) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
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
                        contentDescription = "Aligned with Kaaba",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Allahu Akbar — You are facing the Holy Kaaba!",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Compass Rose Dial with Kaaba Pointer
        Box(
            modifier = Modifier
                .size(310.dp)
                .testTag("qibla_compass_dial"),
            contentAlignment = Alignment.Center
        ) {
            val dialBorderColor = MaterialTheme.colorScheme.outlineVariant
            val tickColor = MaterialTheme.colorScheme.outline
            val northColor = MaterialTheme.colorScheme.primary
            val southColor = MaterialTheme.colorScheme.outline
            val needleColor = MaterialTheme.colorScheme.primary
            val pointerOuter = MaterialTheme.colorScheme.primary
            val pointerInner = MaterialTheme.colorScheme.surface
            val sunIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
            val hubColor = if (isFacingQibla) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            val hubTextColor = if (isFacingQibla) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

            // Rotating Compass Rose Canvas
            Canvas(
                modifier = Modifier
                    .size(290.dp)
                    .rotate(animatedRotation)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f

                // Outer Dial Ring
                drawCircle(
                    color = dialBorderColor,
                    radius = radius,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Degree tick marks (every 10 degrees)
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

                // True North Monochrome Arrow
                val northPath = Path().apply {
                    moveTo(center.x, center.y - radius + 30.dp.toPx())
                    lineTo(center.x - 10.dp.toPx(), center.y - 40.dp.toPx())
                    lineTo(center.x + 10.dp.toPx(), center.y - 40.dp.toPx())
                    close()
                }
                drawPath(path = northPath, color = northColor)

                // South Subtle Arrow
                val southPath = Path().apply {
                    moveTo(center.x, center.y + radius - 30.dp.toPx())
                    lineTo(center.x - 8.dp.toPx(), center.y + 40.dp.toPx())
                    lineTo(center.x + 8.dp.toPx(), center.y + 40.dp.toPx())
                    close()
                }
                drawPath(path = southPath, color = southColor)

                // Kaaba Qibla Pointer Needle
                val qiblaRad = Math.toRadians(qiblaState.qiblaBearing.toDouble())
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

                // Kaaba marker icon at tip of needle
                drawCircle(
                    color = pointerOuter,
                    radius = 9.dp.toPx(),
                    center = Offset(qiblaX, qiblaY)
                )
                drawCircle(
                    color = pointerInner,
                    radius = 4.dp.toPx(),
                    center = Offset(qiblaX, qiblaY)
                )

                // Sun bearing indicator for celestial cross-check
                val sunRad = Math.toRadians(qiblaState.sunAzimuth.toDouble())
                val sunX = center.x + (radius - 20.dp.toPx()) * sin(sunRad).toFloat()
                val sunY = center.y - (radius - 20.dp.toPx()) * cos(sunRad).toFloat()
                drawCircle(color = sunIndicatorColor, radius = 6.dp.toPx(), center = Offset(sunX, sunY))
            }

            // Central Kaaba Hub
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
                    Text(
                        text = "🕋",
                        fontSize = 24.sp
                    )
                    Text(
                        text = "QIBLA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = hubTextColor
                    )
                }
            }
        }

        // Qibla Angle & Kaaba Distance Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Qibla Bearing",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f°", qiblaState.qiblaBearing),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "From True North",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Distance to Kaaba",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f km", qiblaState.distanceKm),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Makkah Al-Mukarramah",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Sun-Based Cross Check Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "☀️", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Sun-Based Celestial Cross-Check",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Current Sun bearing is approx ${String.format(Locale.getDefault(), "%.0f°", qiblaState.sunAzimuth)}. Look at the Sun's physical position outside to confirm device compass orientation.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Manual Bearing Fallback Toggle & Slider
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
                    Text(
                        text = "Manual Bearing Fallback",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedButton(
                        onClick = { manualMode = !manualMode },
                        modifier = Modifier.testTag("toggle_manual_bearing")
                    ) {
                        Text(if (manualMode) "Use Sensors" else "Manual Slider")
                    }
                }

                if (manualMode) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Rotate phone orientation manually: ${manualHeading.toInt()}°",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
            }
        }
    }

    // Figure-8 Motion Calibration Guidance Dialog
    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            title = {
                Text(
                    text = "Sensor Calibration Guidance",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "To ensure high compass accuracy for the Qibla, your phone's magnetometer must be calibrated against magnetic interference:"
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🔄 Figure-8 Motion:",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Hold your phone flat and wave it gently in a continuous Figure-8 (infinity symbol ∞) motion in the air 3 to 5 times.",
                                fontSize = 13.sp
                            )
                        }
                    }
                    Text(
                        text = "• Keep away from large metal objects, speakers, laptop magnets, or thick magnetic phone cases.\n• Keep the phone parallel to the ground while facing the Qibla.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showCalibrationDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }
}
