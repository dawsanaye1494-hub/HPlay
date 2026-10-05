package com.example.ui.dialogs

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppearanceMode
import com.example.model.AudioBufferMode
import com.example.model.AuthMode
import com.example.model.CarPlayConfig
import com.example.ui.theme.ElectricCyan

@Composable
fun SettingsDialog(
    initialConfig: CarPlayConfig,
    onSaveConfig: (CarPlayConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var resPercent by remember { mutableIntStateOf(initialConfig.resolutionPercent) }
    var frameRate by remember { mutableIntStateOf(initialConfig.frameRate) }
    var appearance by remember { mutableStateOf(initialConfig.appearanceMode) }
    var authMode by remember { mutableStateOf(initialConfig.authMode) }
    var audioBuffer by remember { mutableStateOf(initialConfig.audioBufferMode) }

    // Picture Controls
    var brightness by remember { mutableFloatStateOf(initialConfig.brightness) }
    var contrast by remember { mutableFloatStateOf(initialConfig.contrast) }
    var saturation by remember { mutableFloatStateOf(initialConfig.saturation) }
    var tint by remember { mutableFloatStateOf(initialConfig.tint) }

    // Controls
    var wheelMapZoom by remember { mutableStateOf(initialConfig.wheelMapZoom) }
    var joystick by remember { mutableStateOf(initialConfig.mainScreenJoystick) }
    var adbFallback by remember { mutableStateOf(initialConfig.hotspotAdbFallback) }
    var showHudOverlay by remember { mutableStateOf(initialConfig.showHudOverlay) }
    var hudCompact by remember { mutableStateOf(initialConfig.hudOverlayCompact) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = "Settings", tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "H Play Configuration • GAC Hycan Z03",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Resolution Scaling (30% to 160% with correct 30%/160% labels)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            val (calcW, calcH) = CarPlayConfig.calculateResolution(percent = resPercent)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Custom Integer Resolution: $resPercent%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${calcW}x${calcH} px",
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Applying a resolution change will reconnect CarPlay automatically.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Slider(
                                value = resPercent.toFloat(),
                                onValueChange = { resPercent = it.toInt() },
                                valueRange = 30f..160f,
                                steps = 25,
                                colors = SliderDefaults.colors(
                                    thumbColor = ElectricCyan,
                                    activeTrackColor = ElectricCyan,
                                    inactiveTrackColor = Color(0xFF334155)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "30% (Low-RAM Fallback)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(text = "100% (Native 1080p)", fontSize = 11.sp, color = ElectricCyan)
                                Text(text = "160% (Hi-DPI Ultra)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            // Frame Rate selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Frame Rate Target", fontSize = 13.sp, color = Color.White)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(30, 60).forEach { fps ->
                                        val isSel = frameRate == fps
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) ElectricCyan else Color(0xFF334155))
                                                .clickable { frameRate = fps }
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "$fps FPS",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Display Appearance Mode & Live Picture Controls
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Display Appearance & Picture Tuning", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Appearance mode pills
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AppearanceMode.values().forEach { mode ->
                                    val isSel = appearance == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSel) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF0F172A))
                                            .border(1.dp, if (isSel) ElectricCyan else Color(0xFF334155), RoundedCornerShape(8.dp))
                                            .clickable { appearance = mode }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mode.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) ElectricCyan else Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Live Picture Controls: Brightness & Contrast
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Brightness: ${brightness.toInt()}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                                    Slider(
                                        value = brightness,
                                        onValueChange = { brightness = it },
                                        valueRange = -50f..50f,
                                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Contrast: ${String.format("%.2f", contrast)}x", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                                    Slider(
                                        value = contrast,
                                        onValueChange = { contrast = it },
                                        valueRange = 0.5f..1.5f,
                                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                                    )
                                }
                            }

                            // Saturation & Tint
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Saturation: ${String.format("%.2f", saturation)}x", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                                    Slider(
                                        value = saturation,
                                        onValueChange = { saturation = it },
                                        valueRange = 0.0f..2.0f,
                                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Tint: ${tint.toInt()}°", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                                    Slider(
                                        value = tint,
                                        onValueChange = { tint = it },
                                        valueRange = -30f..30f,
                                        colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                                    )
                                }
                            }
                        }
                    }

                    // 3. Audio & Authentication Settings
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Audio Buffering & Crypto Authentication", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))

                            // Audio Buffer Mode
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Audio Buffer", fontSize = 13.sp, color = Color.White)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    AudioBufferMode.values().forEach { buf ->
                                        val isSel = audioBuffer == buf
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) ElectricCyan else Color(0xFF0F172A))
                                                .clickable { audioBuffer = buf }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${buf.latencyMs}ms",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Authentication Selection: Local Software vs USB-CH341
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "CarPlay Auth Protocol", fontSize = 13.sp, color = Color.White)
                                    Text(text = authMode.detail, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    AuthMode.values().forEach { mode ->
                                        val isSel = authMode == mode
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) ElectricCyan else Color(0xFF0F172A))
                                                .clickable { authMode = mode }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = mode.label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.Black else Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 4. Steering Wheel & ADB Hotspot Integration
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(text = "Automotive & CAN Bus Preferences", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Opt-in Wheel Map Zoom", fontSize = 13.sp, color = Color.White)
                                    Text(text = "Scroll wheel zooms map without disrupting volume or calls", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Switch(
                                    checked = wheelMapZoom,
                                    onCheckedChange = { wheelMapZoom = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Main-Screen Joystick", fontSize = 13.sp, color = Color.White)
                                    Text(text = "On-screen directional rotary joystick simulation", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Switch(
                                    checked = joystick,
                                    onCheckedChange = { joystick = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Simplified HUD Overlay", fontSize = 13.sp, color = Color.White)
                                    Text(text = "Displays live speed & turn signals over active CarPlay stream", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Switch(
                                    checked = showHudOverlay,
                                    onCheckedChange = { showHudOverlay = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "HUD Minimalist / Compact Mode", fontSize = 13.sp, color = Color.White)
                                    Text(text = "Minimalist floating speed pill instead of full card", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Switch(
                                    checked = hudCompact,
                                    onCheckedChange = { hudCompact = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Authorized ADB Hotspot Fallback", fontSize = 13.sp, color = Color.White)
                                    Text(text = "Recovers car-hotspot state on supported vehicle firmware", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Switch(
                                    checked = adbFallback,
                                    onCheckedChange = { adbFallback = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ElectricCyan)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons (Save vs Cancel)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Cancel", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val updated = initialConfig.copy(
                                resolutionPercent = resPercent,
                                frameRate = frameRate,
                                appearanceMode = appearance,
                                authMode = authMode,
                                audioBufferMode = audioBuffer,
                                brightness = brightness,
                                contrast = contrast,
                                saturation = saturation,
                                tint = tint,
                                wheelMapZoom = wheelMapZoom,
                                mainScreenJoystick = joystick,
                                hotspotAdbFallback = adbFallback,
                                showHudOverlay = showHudOverlay,
                                hudOverlayCompact = hudCompact
                            )
                            onSaveConfig(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Apply & Save", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    }
}
