package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GearPosition
import com.example.model.VehicleTelemetry
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EvGreen

/**
 * Simplified, floating Heads-Up Display (HUD) overlay component.
 * Stays visible on top of the active Apple CarPlay streaming interface
 * to keep safety-critical vehicle metrics (speed, blinking turn signals, gear, battery)
 * accessible at all times without leaving CarPlay navigation or media.
 */
@Composable
fun AutomotiveHudOverlay(
    telemetry: VehicleTelemetry,
    isCompact: Boolean = false,
    onToggleCompact: () -> Unit = {},
    onToggleLeftSignal: () -> Unit = {},
    onToggleRightSignal: () -> Unit = {},
    onToggleHazard: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 750ms standard automotive flasher cadence
    val infiniteTransition = rememberInfiniteTransition(label = "turn_signal_flasher")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 375, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink_alpha"
    )

    val isLeftActive = telemetry.isLeftTurnSignalOn || telemetry.isHazardLightOn
    val isRightActive = telemetry.isRightTurnSignalOn || telemetry.isHazardLightOn

    val isOverSpeed = telemetry.speedKmh > telemetry.speedLimitKmh
    val speedColor by animateColorAsState(
        targetValue = if (isOverSpeed) Color(0xFFEF4444) else Color.White,
        label = "speed_color"
    )

    Box(
        modifier = modifier
            .shadow(16.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xE6030712)) // Translucent dark glass
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        if (isOverSpeed) Color(0xFFEF4444).copy(alpha = 0.6f) else ElectricCyan.copy(alpha = 0.4f),
                        Color(0xFF1E293B).copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("automotive_hud_overlay")
    ) {
        if (isCompact) {
            // Minimalist compact HUD pill (Speed + Turn Signals + Gear)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Left Turn Indicator
                TurnSignalIndicator(
                    isLeft = true,
                    isActive = isLeftActive,
                    blinkAlpha = blinkAlpha,
                    onClick = onToggleLeftSignal,
                    modifier = Modifier.size(28.dp)
                )

                // Digital Speed
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.clickable { onToggleCompact() }
                ) {
                    Text(
                        text = telemetry.speedKmh.toInt().toString(),
                        fontSize = 28.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = speedColor
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "KM/H",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Gear Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (telemetry.gear) {
                                GearPosition.D -> EvGreen.copy(alpha = 0.25f)
                                GearPosition.R -> Color(0xFFF59E0B).copy(alpha = 0.25f)
                                else -> Color(0xFF1E293B)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = telemetry.gear.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = when (telemetry.gear) {
                            GearPosition.D -> EvGreen
                            GearPosition.R -> Color(0xFFF59E0B)
                            else -> Color.White
                        }
                    )
                }

                // Right Turn Indicator
                TurnSignalIndicator(
                    isLeft = false,
                    isActive = isRightActive,
                    blinkAlpha = blinkAlpha,
                    onClick = onToggleRightSignal,
                    modifier = Modifier.size(28.dp)
                )

                // Expand Button
                IconButton(
                    onClick = onToggleCompact,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand HUD",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            // Full Featured HUD Overlay Card
            Column {
                // Top Row: Controls & Signal Blinker Header
                Row(
                    modifier = Modifier.width(280.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (telemetry.canBusActive) EvGreen else Color(0xFFEF4444))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HUD OVERLAY • GAC Z03",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = onToggleCompact,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Minimize HUD",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close HUD",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Center Stage: Left Signal < Speed > Right Signal
                Row(
                    modifier = Modifier.width(280.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Turn Signal Arrow
                    TurnSignalIndicator(
                        isLeft = true,
                        isActive = isLeftActive,
                        blinkAlpha = blinkAlpha,
                        onClick = onToggleLeftSignal,
                        modifier = Modifier.size(40.dp)
                    )

                    // Core Speed Display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onToggleCompact() }
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = telemetry.speedKmh.toInt().toString(),
                                fontSize = 42.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = speedColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "km/h",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        // Speed limit indicator badge
                        if (isOverSpeed) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.25f))
                                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SPEED LIMIT ${telemetry.speedLimitKmh}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    // Right Turn Signal Arrow
                    TurnSignalIndicator(
                        isLeft = false,
                        isActive = isRightActive,
                        blinkAlpha = blinkAlpha,
                        onClick = onToggleRightSignal,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Lower Info Bar: Gear Selector + Battery SoC + Hazard Button
                Row(
                    modifier = Modifier.width(280.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gear Selector Strip
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F172A))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf(GearPosition.P, GearPosition.R, GearPosition.N, GearPosition.D).forEach { g ->
                            val isSelected = telemetry.gear == g
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isSelected) {
                                            when (g) {
                                                GearPosition.D -> EvGreen
                                                GearPosition.R -> Color(0xFFF59E0B)
                                                GearPosition.P -> Color(0xFFEF4444)
                                                GearPosition.N -> ElectricCyan
                                            }
                                        } else Color.Transparent
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = g.name,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // Battery & Range Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricBolt,
                            contentDescription = "EV Battery",
                            tint = EvGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${telemetry.batterySocPercent}% • ${telemetry.estimatedRangeKm}km",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Hazard Flasher Toggle Button
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (telemetry.isHazardLightOn) Color(0xFFEF4444) else Color(0xFF1E293B))
                            .clickable { onToggleHazard() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Hazard Warning Lights",
                            tint = if (telemetry.isHazardLightOn) Color.Black else Color(0xFFEF4444),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TurnSignalIndicator(
    isLeft: Boolean,
    isActive: Boolean,
    blinkAlpha: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAlpha = if (isActive) blinkAlpha else 0.2f
    val iconColor = if (isActive) EvGreen else Color(0xFF64748B)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) EvGreen.copy(alpha = 0.15f) else Color(0xFF0A0F1D))
            .border(
                1.dp,
                if (isActive) EvGreen.copy(alpha = activeAlpha) else Color(0xFF1E293B),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .alpha(if (isActive) activeAlpha else 0.4f),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isLeft) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = if (isLeft) "Left Turn Signal" else "Right Turn Signal",
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
    }
}
