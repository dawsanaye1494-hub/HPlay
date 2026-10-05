package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.GearPosition
import com.example.model.VehicleTelemetry
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EvGreen

@Composable
fun CanBusMonitorDialog(
    telemetry: VehicleTelemetry,
    rawFrames: List<String>,
    onSetGear: (GearPosition) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .testTag("can_bus_monitor_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B14))
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DeveloperBoard, contentDescription = "CAN", tint = ElectricCyan)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GAC Hycan Z03 • Automotive CAN Bus Hub",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Device: ${telemetry.serialPort} • R-Car Renesas G6SA-r8a7796",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElectricCyan
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Telemetry Quick Overview Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Speed", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(text = "${telemetry.speedKmh.toInt()} km/h", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text(text = "Gear", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(text = telemetry.gear.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ElectricCyan)
                    }
                    Column {
                        Text(text = "BMS SoC", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(text = "${telemetry.batterySocPercent}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EvGreen)
                    }
                    Column {
                        Text(text = "Pack Volts", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(text = "${telemetry.batteryVoltageV} V", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text(text = "Ambient Lux", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(text = "${telemetry.lightSensorLux.toInt()} lx", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "LIVE CAN BUS FRAMES (ISO 11898-2 / 500 kbps)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Raw Frame Terminal
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF020408)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    ) {
                        items(rawFrames) { frame ->
                            Text(
                                text = frame,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (frame.contains("0x180")) ElectricCyan else if (frame.contains("0x310")) EvGreen else Color(0xFFCBD5E1),
                                modifier = Modifier.padding(vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shifter Override controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "CAN Shifter Override:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GearPosition.values().forEach { g ->
                            Button(
                                onClick = { onSetGear(g) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (telemetry.gear == g) ElectricCyan else Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = g.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (telemetry.gear == g) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
