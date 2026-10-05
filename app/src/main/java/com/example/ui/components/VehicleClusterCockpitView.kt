package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GearPosition
import com.example.model.TireData
import com.example.model.VehicleTelemetry
import com.example.ui.theme.BatteryOrange
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EvGreen
import kotlin.math.cos
import kotlin.math.sin

/**
 * Automotive Cockpit Composable displaying:
 * 1. Vehicle Speed with real-time analog/digital dial gauge
 * 2. Traction Battery level, power flow (kW), and range
 * 3. Tire Pressure Monitoring System (TPMS) across all 4 wheels
 * Powered by real-time CAN bus telemetry (IDs 0x220, 0x310, 0x360).
 */
@Composable
fun VehicleClusterCockpitView(
    telemetry: VehicleTelemetry,
    onSetGear: ((GearPosition) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showPsi by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .testTag("vehicle_cluster_cockpit_view"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar: CAN Protocol Status & Vehicle Model
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Car",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = telemetry.vehicleModel,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "CAN Bus: Active • 500 kbps (${telemetry.serialPort})",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = ElectricCyan
                        )
                    }
                }

                // Gear Pill Selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GearPosition.values().forEach { g ->
                        val isSelected = telemetry.gear == g
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) {
                                        if (g == GearPosition.P) Color(0xFFEF4444) else ElectricCyan
                                    } else Color(0xFF1E293B)
                                )
                                .clickable { onSetGear?.invoke(g) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = g.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.Black else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Central Area: Speedometer Gauge + Battery Status + TPMS Layout
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Vehicle Speedometer Gauge
                SpeedometerPanel(
                    speedKmh = telemetry.speedKmh,
                    motorRpm = telemetry.motorRpm,
                    modifier = Modifier.weight(0.33f)
                )

                // Section 2: EV Battery Level & Power Flow
                BatteryPowerPanel(
                    telemetry = telemetry,
                    modifier = Modifier.weight(0.33f)
                )

                // Section 3: Tire Pressure Monitoring (TPMS)
                TpmsPanel(
                    tpms = telemetry.tpms,
                    showPsi = showPsi,
                    onToggleUnit = { showPsi = !showPsi },
                    modifier = Modifier.weight(0.34f)
                )
            }
        }
    }
}

/**
 * High-precision circular Speedometer dial with graduated arc
 */
@Composable
fun SpeedometerPanel(
    speedKmh: Float,
    motorRpm: Int,
    modifier: Modifier = Modifier
) {
    val animatedSpeed by animateFloatAsState(
        targetValue = speedKmh,
        animationSpec = tween(durationMillis = 200),
        label = "speed_anim"
    )

    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SPEEDOMETER (CAN 0x220)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                letterSpacing = 1.sp
            )

            // Speed Dial Canvas
            Box(
                modifier = Modifier
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 12f
                    val arcSize = size.width - strokeW * 2
                    val startAngle = 140f
                    val sweepAngle = 260f

                    // Background track arc
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = Offset(strokeW, strokeW),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )

                    // Active speed gradient arc
                    val progressRatio = (animatedSpeed / 180f).coerceIn(0f, 1f)
                    val activeSweep = sweepAngle * progressRatio
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(ElectricBlue, ElectricCyan, Color(0xFF38BDF8))
                        ),
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(strokeW, strokeW),
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedSpeed.toInt()}",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    Text(
                        text = "KM/H",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Motor RPM
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF070B14))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Motor", fontSize = 10.sp, color = Color(0xFF64748B))
                Text(
                    text = "$motorRpm RPM",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}

/**
 * EV Traction Battery SoC and Power Flow Meter
 */
@Composable
fun BatteryPowerPanel(
    telemetry: VehicleTelemetry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRACTION BATTERY (0x310)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = Icons.Default.BatteryChargingFull,
                    contentDescription = "Battery",
                    tint = EvGreen,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Main Percentage & Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${telemetry.batterySocPercent}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = EvGreen,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "STATE OF CHARGE",
                        fontSize = 9.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${telemetry.estimatedRangeKm} km",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "EST. RANGE",
                        fontSize = 9.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Battery SoC Progress Bar
            LinearProgressIndicator(
                progress = { (telemetry.batterySocPercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (telemetry.batterySocPercent < 20) Color(0xFFEF4444) else EvGreen,
                trackColor = Color(0xFF1E293B)
            )

            // Power Flow (kW)
            val isRegen = telemetry.powerKw < 0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF070B14))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Power",
                        tint = if (isRegen) EvGreen else ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRegen) "REGEN POWER" else "DISCHARGE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                }
                Text(
                    text = "${telemetry.powerKw} kW",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (isRegen) EvGreen else ElectricCyan
                )
            }

            // Voltage & Pack Temp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pack: ${telemetry.batteryVoltageV}V",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Temp: ${telemetry.batteryTempC}°C",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * 4-Wheel Tire Pressure Monitoring System (TPMS) display
 */
@Composable
fun TpmsPanel(
    tpms: com.example.model.TpmsStatus,
    showPsi: Boolean,
    onToggleUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with unit toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TIRE PRESSURE (0x360)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 0.5.sp
                )

                // Unit Toggle Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .clickable { onToggleUnit() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (showPsi) "PSI" else "BAR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }

            // Chassis 4-Tire Grid Layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Front Axle: FL and FR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TireBadge(
                        label = "FRONT L",
                        data = tpms.frontLeft,
                        showPsi = showPsi,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TireBadge(
                        label = "FRONT R",
                        data = tpms.frontRight,
                        showPsi = showPsi,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Rear Axle: RL and RR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TireBadge(
                        label = "REAR L",
                        data = tpms.rearLeft,
                        showPsi = showPsi,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TireBadge(
                        label = "REAR R",
                        data = tpms.rearRight,
                        showPsi = showPsi,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Status summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF070B14))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "OK",
                        tint = EvGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "All 4 Tires Calibrated",
                        fontSize = 9.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
                Text(
                    text = "CAN SYNC OK",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = EvGreen
                )
            }
        }
    }
}

@Composable
fun TireBadge(
    label: String,
    data: TireData,
    showPsi: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF070B14))
            .border(
                width = 1.dp,
                color = if (data.isWarning) Color(0xFFEF4444) else ElectricCyan.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                Text(text = "${data.tempC}°C", fontSize = 8.sp, color = Color(0xFF94A3B8))
            }
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (showPsi) "${data.pressurePsi}" else String.format("%.2f", data.pressureBar),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (data.isWarning) Color(0xFFEF4444) else Color.White
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (showPsi) "psi" else "bar",
                    fontSize = 9.sp,
                    color = ElectricCyan,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}
