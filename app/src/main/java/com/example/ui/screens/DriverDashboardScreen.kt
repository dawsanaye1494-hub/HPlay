package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ModeFanOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AirVentMode
import com.example.model.AppearanceMode
import com.example.model.GearPosition
import com.example.model.VehicleTelemetry
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EvGreen

/**
 * High-contrast, driver-focused dashboard screen built with Jetpack Compose.
 * Displays real-time speed, battery percentage, and climate control status
 * received directly from the CAN bus (0x220, 0x310, 0x3B0).
 *
 * Supports an automatic 'Night Mode' toggle that adjusts the dashboard color palette
 * (using pure OLED black, softened amber/sky accents, and reduced luminescence)
 * to eliminate glare and preserve dark adaptation for night driving.
 */
@Composable
fun DriverDashboardScreen(
    telemetry: VehicleTelemetry,
    isNightMode: Boolean = true,
    appearanceMode: AppearanceMode = AppearanceMode.NIGHT,
    onToggleNightMode: () -> Unit = {},
    onSetAppearanceMode: (AppearanceMode) -> Unit = {},
    onSetGear: (GearPosition) -> Unit,
    onAdjustClimateTemp: (Float) -> Unit,
    onSetFanSpeed: (Int) -> Unit,
    onToggleAc: () -> Unit,
    onToggleAutoClimate: () -> Unit,
    onToggleRecirculation: () -> Unit,
    onToggleFrontDefrost: () -> Unit,
    onToggleRearDefrost: () -> Unit,
    onCycleVentMode: () -> Unit,
    onCycleSeatHeating: () -> Unit,
    onSetDriveMode: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Smooth animated palette transitions to prevent abrupt eye-dazzle when mode changes
    val bgColor by animateColorAsState(
        targetValue = if (isNightMode) Color(0xFF000000) else Color(0xFF070F20),
        animationSpec = tween(durationMillis = 350),
        label = "dashboard_bg"
    )
    val cardColor by animateColorAsState(
        targetValue = if (isNightMode) Color(0xFF050811) else Color(0xFF0D1B36),
        animationSpec = tween(durationMillis = 350),
        label = "dashboard_card"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isNightMode) Color(0xFF1E293B) else ElectricCyan.copy(alpha = 0.6f),
        animationSpec = tween(durationMillis = 350),
        label = "dashboard_border"
    )
    val primaryAccent by animateColorAsState(
        targetValue = if (isNightMode) Color(0xFF38BDF8) else ElectricCyan,
        animationSpec = tween(durationMillis = 350),
        label = "dashboard_primary"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("driver_dashboard_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Top Bar: Navigation & CAN Bus Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F2937))
                            .testTag("dashboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Return",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "GAC HYCAN Z03",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(EvGreen.copy(alpha = 0.2f))
                                    .border(1.dp, EvGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "CAN BUS ONLINE",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = EvGreen
                                )
                            }
                        }
                        Text(
                            text = "Driver Cockpit HUD • ${telemetry.serialPort}",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Quick Mode Selectors & Night Mode Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Drive Mode Selector
                    listOf("ECO", "NORMAL", "SPORT").forEach { mode ->
                        val isCurrent = telemetry.driveMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isCurrent) {
                                        when (mode) {
                                            "SPORT" -> Color(0xFFEF4444)
                                            "NORMAL" -> ElectricBlue
                                            else -> EvGreen
                                        }
                                    } else Color(0xFF1E293B)
                                )
                                .clickable { onSetDriveMode(mode) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = mode,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isCurrent) Color.Black else Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Night Mode Anti-Glare Toggle Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isNightMode) Color(0xFF0F172A) else Color(0xFF0284C7).copy(alpha = 0.2f))
                            .border(1.dp, if (isNightMode) Color(0xFF38BDF8) else Color(0xFF0284C7), RoundedCornerShape(8.dp))
                            .clickable { onToggleNightMode() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("night_mode_toggle_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (appearanceMode) {
                                    AppearanceMode.NIGHT -> Icons.Default.DarkMode
                                    AppearanceMode.DAY -> Icons.Default.LightMode
                                    AppearanceMode.LIGHT_SENSOR -> Icons.Default.BrightnessAuto
                                    AppearanceMode.SYSTEM -> Icons.Default.DarkMode
                                },
                                contentDescription = "Night Mode Toggle",
                                tint = if (isNightMode) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = when (appearanceMode) {
                                        AppearanceMode.NIGHT -> "NIGHT: ANTI-GLARE"
                                        AppearanceMode.DAY -> "DAY: HIGH CONTRAST"
                                        AppearanceMode.LIGHT_SENSOR -> "AUTO (CAN SENSOR)"
                                        AppearanceMode.SYSTEM -> "SYSTEM NIGHT"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isNightMode) Color(0xFF38BDF8) else Color.White
                                )
                                Text(
                                    text = if (appearanceMode == AppearanceMode.LIGHT_SENSOR) {
                                        "${telemetry.lightSensorLux.toInt()} lx • ${if (telemetry.isHeadlightOn) "Lights ON" else "Lights OFF"}"
                                    } else {
                                        if (isNightMode) "Zero Windshield Glare" else "Full Daylight Contrast"
                                    },
                                    fontSize = 8.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Dashboard Tri-Pane (Speedometer + Battery + Climate)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Real-Time Speed & Transmission
                SpeedSection(
                    telemetry = telemetry,
                    onSetGear = onSetGear,
                    cardColor = cardColor,
                    borderColor = borderColor,
                    modifier = Modifier.weight(0.33f)
                )

                // Section 2: High-Voltage Traction Battery & Range
                BatterySection(
                    telemetry = telemetry,
                    cardColor = cardColor,
                    borderColor = borderColor,
                    modifier = Modifier.weight(0.33f)
                )

                // Section 3: CAN Climate Control Status & Direct HVAC Controls
                ClimateControlSection(
                    climate = telemetry.climate,
                    onAdjustTemp = onAdjustClimateTemp,
                    onSetFanSpeed = onSetFanSpeed,
                    onToggleAc = onToggleAc,
                    onToggleAuto = onToggleAutoClimate,
                    onToggleRecirc = onToggleRecirculation,
                    onToggleFrontDefrost = onToggleFrontDefrost,
                    onToggleRearDefrost = onToggleRearDefrost,
                    onCycleVent = onCycleVentMode,
                    onCycleHeating = onCycleSeatHeating,
                    cardColor = cardColor,
                    borderColor = borderColor,
                    modifier = Modifier.weight(0.34f)
                )
            }
        }
    }
}

/**
 * High-visibility Speedometer with sweeping speed gauge and gear pill selector
 */
@Composable
fun SpeedSection(
    telemetry: VehicleTelemetry,
    onSetGear: (GearPosition) -> Unit,
    cardColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = telemetry.speedKmh,
        animationSpec = tween(durationMillis = 200),
        label = "hud_speed"
    )

    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Speed Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SPEED (CAN 0x220)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${telemetry.motorRpm} RPM",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )
            }

            // Radial Speed Arc
            Box(
                modifier = Modifier.size(145.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14f
                    val arcSize = size.width - strokeWidth * 2
                    val startAngle = 135f
                    val sweepAngle = 270f

                    // Background Track
                    drawArc(
                        color = Color(0xFF1F2937),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeWidth, strokeWidth),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active Speed Arc
                    val ratio = (animatedSpeed / 180f).coerceIn(0f, 1f)
                    val activeSweep = sweepAngle * ratio
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(ElectricBlue, ElectricCyan, Color(0xFF38BDF8), Color(0xFF67E8F9))
                        ),
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(strokeWidth, strokeWidth),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedSpeed.toInt()}",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    Text(
                        text = "KM / H",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Gear Selector Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF030712))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GearPosition.values().forEach { g ->
                    val isSelected = telemetry.gear == g
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) {
                                    if (g == GearPosition.P) Color(0xFFEF4444) else ElectricCyan
                                } else Color.Transparent
                            )
                            .clickable { onSetGear(g) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = g.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) Color.Black else Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Real-time EV Traction Battery, Power Flow, and Driving Range
 */
@Composable
fun BatterySection(
    telemetry: VehicleTelemetry,
    cardColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = "Battery",
                        tint = EvGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "BATTERY (CAN 0x310)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "${telemetry.batteryTempC}°C",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )
            }

            // Big SoC Percentage & Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${telemetry.batterySocPercent}%",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = EvGreen
                    )
                    Text(
                        text = "STATE OF CHARGE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${telemetry.estimatedRangeKm}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "EST. RANGE (KM)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { telemetry.batterySocPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = if (telemetry.batterySocPercent < 20) Color(0xFFEF4444) else EvGreen,
                trackColor = Color(0xFF1F2937)
            )

            // Power Flow Meter (Regen vs Discharge)
            val isRegen = telemetry.powerKw < 0
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF030712))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "Power",
                            tint = if (isRegen) EvGreen else ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRegen) "REGENERATIVE" else "DISCHARGE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Text(
                        text = "${telemetry.powerKw} kW",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = if (isRegen) EvGreen else ElectricCyan
                    )
                }
            }

            // High-Voltage Pack Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "VOLTAGE", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text(text = "${telemetry.batteryVoltageV} V", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "CURRENT", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text(text = "${telemetry.batteryCurrentA} A", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "ODOMETER", fontSize = 9.sp, color = Color(0xFF64748B))
                    Text(text = "${telemetry.odoKm} km", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

/**
 * Climate Control Status received from CAN bus (0x3B0) with driver-accessible controls
 */
@Composable
fun ClimateControlSection(
    climate: com.example.model.ClimateTelemetry,
    onAdjustTemp: (Float) -> Unit,
    onSetFanSpeed: (Int) -> Unit,
    onToggleAc: () -> Unit,
    onToggleAuto: () -> Unit,
    onToggleRecirc: () -> Unit,
    onToggleFrontDefrost: () -> Unit,
    onToggleRearDefrost: () -> Unit,
    onCycleVent: () -> Unit,
    onCycleHeating: () -> Unit,
    cardColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = "Climate",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CLIMATE (CAN 0x3B0)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = if (climate.isAutoMode) "AUTO ON" else "MANUAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (climate.isAutoMode) EvGreen else ElectricCyan
                )
            }

            // Temperature Controls (Driver Dual-Zone)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF030712))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrement Button (>= 48dp target)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { onAdjustTemp(-0.5f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Lower Temp",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format("%.1f", climate.driverTempC)}°C",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    Text(
                        text = "TARGET TEMP (SYNC)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                // Increment Button (>= 48dp target)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { onAdjustTemp(0.5f) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Raise Temp",
                        tint = Color.White
                    )
                }
            }

            // Fan Speed Stepped Meter (1 to 7)
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "BLOWER SPEED", fontSize = 9.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Text(text = "LVL ${climate.fanSpeed} / 7", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 1..7) {
                        val active = i <= climate.fanSpeed
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (active) ElectricCyan else Color(0xFF1E293B))
                                .clickable { onSetFanSpeed(i) }
                        )
                    }
                }
            }

            // Climate Quick-Action Buttons (AC, Recirc, Defrost, Vent)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ClimateTogglePill(
                    label = "A/C",
                    isActive = climate.isAcOn,
                    onClick = onToggleAc,
                    modifier = Modifier.weight(1f)
                )
                ClimateTogglePill(
                    label = "AUTO",
                    isActive = climate.isAutoMode,
                    onClick = onToggleAuto,
                    modifier = Modifier.weight(1f)
                )
                ClimateTogglePill(
                    label = if (climate.isRecirculation) "RECIRC" else "FRESH",
                    isActive = climate.isRecirculation,
                    onClick = onToggleRecirc,
                    modifier = Modifier.weight(1.2f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ClimateTogglePill(
                    label = "DEFROST",
                    isActive = climate.isFrontDefrostOn,
                    onClick = onToggleFrontDefrost,
                    modifier = Modifier.weight(1f)
                )
                ClimateTogglePill(
                    label = climate.ventMode.label,
                    isActive = true,
                    onClick = onCycleVent,
                    modifier = Modifier.weight(1f)
                )
                ClimateTogglePill(
                    label = "HEAT: ${climate.seatHeatingDriver}",
                    isActive = climate.seatHeatingDriver > 0,
                    onClick = onCycleHeating,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ClimateTogglePill(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (isActive) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF030712),
        label = "pill_bg"
    )
    val borderCol by animateColorAsState(
        targetValue = if (isActive) ElectricCyan else Color(0xFF1E293B),
        label = "pill_border"
    )

    Box(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) ElectricCyan else Color(0xFF94A3B8)
        )
    }
}
