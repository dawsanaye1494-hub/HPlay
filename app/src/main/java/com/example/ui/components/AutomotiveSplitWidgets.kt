package com.example.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CarPlayConfig
import com.example.model.CarPlaySession
import com.example.model.GearPosition
import com.example.model.SplitWidget
import com.example.model.VehicleTelemetry
import com.example.ui.theme.BatteryOrange
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EvGreen

@Composable
fun AutomotiveSplitWidgets(
    config: CarPlayConfig,
    session: CarPlaySession,
    telemetry: VehicleTelemetry,
    onSelectWidget: (SplitWidget) -> Unit,
    onWheelButton: (String, Int) -> Unit,
    onSetGear: (GearPosition) -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .testTag("automotive_split_widgets"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Widget Tabs Header
            val tabs = listOf(
                Pair(SplitWidget.VEHICLE_CAN, "CAN Hub"),
                Pair(SplitWidget.NAVIGATION_HUD, "Cluster HUD"),
                Pair(SplitWidget.MEDIA_NOW_PLAYING, "Audio"),
                Pair(SplitWidget.DRIVE_ASSIST, "Controls")
            )
            val selectedIndex = tabs.indexOfFirst { it.first == config.selectedSplitWidget }.coerceAtLeast(0)

            TabRow(
                selectedTabIndex = selectedIndex,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = ElectricCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                        color = ElectricCyan,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, (widget, label) ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { onSelectWidget(widget) },
                        modifier = Modifier.height(42.dp),
                        text = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedIndex == index) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Widget Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                when (config.selectedSplitWidget) {
                    SplitWidget.VEHICLE_CAN -> VehicleCanTelemetryWidget(
                        telemetry = telemetry,
                        onSetGear = onSetGear
                    )
                    SplitWidget.NAVIGATION_HUD -> NavigationHudWidget(
                        turn = session.navigation,
                        speedKmh = telemetry.speedKmh
                    )
                    SplitWidget.MEDIA_NOW_PLAYING -> MediaSyncWidget(
                        media = session.media,
                        metrics = session.metrics,
                        audioMode = config.audioBufferMode.label,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrev = onPrev
                    )
                    SplitWidget.DRIVE_ASSIST -> SteeringControlsWidget(
                        telemetry = telemetry,
                        onWheelButton = onWheelButton
                    )
                }
            }
        }
    }
}

@Composable
fun VehicleCanTelemetryWidget(
    telemetry: VehicleTelemetry,
    onSetGear: (GearPosition) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Digital Speed & Motor RPM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "VEHICLE SPEED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${telemetry.speedKmh.toInt()}",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "KM/H",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Gear Selector Buttons (CAN Simulation/Override)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "TRANSMISSION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    GearPosition.values().forEach { g ->
                        val isSelected = telemetry.gear == g
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSelected) {
                                        if (g == GearPosition.P) Color(0xFFEF4444) else ElectricCyan
                                    } else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onSetGear(g) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = g.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // EV Battery Stats Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Traction Battery SoC", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "${telemetry.batterySocPercent}% (${telemetry.estimatedRangeKm} km)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EvGreen)
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = { telemetry.batterySocPercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = EvGreen,
                    trackColor = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStat(label = "Pack", value = "${telemetry.batteryVoltageV.toInt()}V")
                    MiniStat(label = "Current", value = "${telemetry.batteryCurrentA}A")
                    MiniStat(label = "Power", value = "${telemetry.powerKw}kW")
                    MiniStat(label = "Temp", value = "${telemetry.batteryTempC.toInt()}°C")
                }
            }
        }

        // Tire Pressure Monitoring System (TPMS 0x360) 4-Wheel Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIRE PRESSURE (CAN 0x360)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "4-WHEEL OK",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EvGreen
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniTireBadge(label = "FL", bar = telemetry.tpms.frontLeft.pressureBar, temp = telemetry.tpms.frontLeft.tempC)
                    MiniTireBadge(label = "FR", bar = telemetry.tpms.frontRight.pressureBar, temp = telemetry.tpms.frontRight.tempC)
                    MiniTireBadge(label = "RL", bar = telemetry.tpms.rearLeft.pressureBar, temp = telemetry.tpms.rearLeft.tempC)
                    MiniTireBadge(label = "RR", bar = telemetry.tpms.rearRight.pressureBar, temp = telemetry.tpms.rearRight.tempC)
                }
            }
        }

        // CAN Bus Status Footer
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
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(EvGreen))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "CAN Bus: Active", fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
            Text(text = telemetry.serialPort, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = ElectricCyan)
        }
    }
}

@Composable
fun MiniStat(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 9.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE2E8F0))
    }
}

@Composable
fun NavigationHudWidget(
    turn: com.example.model.NavigationTurn,
    speedKmh: Float
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // High visibility Cluster HUD Next Maneuver
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.5.dp, ElectricCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Turn Arrow",
                            tint = ElectricCyan,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = turn.distanceText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = turn.streetName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = turn.nextManeuver,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // Trip Progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "DESTINATION ETA", fontSize = 10.sp, color = Color(0xFF64748B))
                Text(text = turn.etaText, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "REMAINING", fontSize = 10.sp, color = Color(0xFF64748B))
                Text(text = "${turn.remainingDistanceKm} km (${turn.remainingTimeMin}m)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
            }
        }

        // Lane Guidance Visualization
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF070B14))
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            turn.laneGuidance.forEachIndexed { i, active ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(24.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) ElectricCyan.copy(alpha = 0.25f) else Color(0xFF1E293B))
                        .border(1.dp, if (active) ElectricCyan else Color(0xFF334155), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Lane $i",
                        tint = if (active) ElectricCyan else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MediaSyncWidget(
    media: com.example.model.MediaMetadata,
    metrics: com.example.model.StreamMetrics,
    audioMode: String,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFFA2D48), Color(0xFF7A092A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = "Art", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = media.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Text(text = media.artist, fontSize = 12.sp, color = ElectricCyan, maxLines = 1)
                Text(text = media.album, fontSize = 10.sp, color = Color(0xFF94A3B8), maxLines = 1)
            }
        }

        // Audio Track Buffer Indicator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Audio Buffer: $audioMode", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(text = "${metrics.audioBufferDepthMs} ms", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = EvGreen)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (metrics.audioBufferDepthMs / 300f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = ElectricCyan,
                    trackColor = Color(0xFF1E293B)
                )
            }
        }

        // Media Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan)
                    .clickable { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (media.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onNext, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
fun SteeringControlsWidget(
    telemetry: VehicleTelemetry,
    onWheelButton: (String, Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "STEERING WHEEL CAN INTERFACE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Steering Wheel Test Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WheelTestButton(
                icon = Icons.Default.VolumeUp,
                label = "Vol+",
                onClick = { onWheelButton("VOL_UP", 24) }
            )
            WheelTestButton(
                icon = Icons.Default.VolumeDown,
                label = "Vol-",
                onClick = { onWheelButton("VOL_DOWN", 25) }
            )
            WheelTestButton(
                icon = Icons.Default.SkipPrevious,
                label = "Prev",
                onClick = { onWheelButton("PREV", 88) }
            )
            WheelTestButton(
                icon = Icons.Default.SkipNext,
                label = "Next",
                onClick = { onWheelButton("NEXT", 87) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            WheelTestButton(
                icon = Icons.Default.Call,
                label = "Call",
                onClick = { onWheelButton("CALL", 5) }
            )
            WheelTestButton(
                icon = Icons.Default.CallEnd,
                label = "End",
                onClick = { onWheelButton("END_CALL", 6) }
            )
            WheelTestButton(
                icon = Icons.Default.Mic,
                label = "Voice",
                onClick = { onWheelButton("VOICE", 84) }
            )
        }

        // Ambient Light Sensor Lux
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF070B14))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Light Sensor (CAN 0x380)", fontSize = 10.sp, color = Color(0xFF94A3B8))
            Text(text = "${telemetry.lightSensorLux.toInt()} lux (${if (telemetry.isHeadlightOn) "Lights ON" else "Lights OFF"})", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
        }
    }
}

@Composable
fun WheelTestButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, ElectricCyan.copy(alpha = 0.3f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun MiniTireBadge(label: String, bar: Float, temp: Int) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF070B14))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, fontSize = 8.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
            Text(
                text = "${String.format("%.1f", bar)}b",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

