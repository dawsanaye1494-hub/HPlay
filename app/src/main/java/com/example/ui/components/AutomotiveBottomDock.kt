package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.FlipToFront
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.model.CarPlayConfig
import com.example.model.CarPlaySession
import com.example.model.ConnectionMode
import com.example.model.SessionState
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EvGreen

@Composable
fun AutomotiveBottomDock(
    session: CarPlaySession,
    config: CarPlayConfig,
    onSelectMode: (ConnectionMode) -> Unit,
    onToggleSplit: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCanMonitor: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenCockpit: () -> Unit,
    onOpenLogViewer: () -> Unit,
    onToggleHudOverlay: (() -> Unit)? = null,
    onReconnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp)
            .testTag("automotive_bottom_dock")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Selectors (Wi-Fi Direct, Car Hotspot, Same LAN, Wired USB)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LINK:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ConnectionMode.values().forEach { mode ->
                    val isSelected = config.connectionMode == mode
                    val shortName = when (mode) {
                        ConnectionMode.WIFI_DIRECT -> "Wi-Fi Direct"
                        ConnectionMode.CAR_HOTSPOT -> "Hotspot"
                        ConnectionMode.SAME_LAN -> "Same LAN"
                        ConnectionMode.WIRED_USB -> "Wired USB"
                    }
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) ElectricCyan.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) ElectricCyan else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectMode(mode) }
                            .padding(horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shortName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) ElectricCyan else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Central Status Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF070B14))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (session.state) {
                                SessionState.STREAMING -> EvGreen
                                SessionState.AUTHENTICATING, SessionState.SCANNING, SessionState.RECONNECTING -> ElectricCyan
                                else -> Color(0xFFEF4444)
                            }
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${session.state.label} • ${session.phoneInfo.name} (${session.metrics.resolutionFormatted})",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )
            }

            // Quick Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Split Screen toggle
                IconButton(
                    onClick = onToggleSplit,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (config.splitScreenEnabled) ElectricCyan.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewSidebar,
                        contentDescription = "Split Screen",
                        tint = if (config.splitScreenEnabled) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Settings
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // CAN Bus Monitor
                IconButton(
                    onClick = onOpenCanMonitor,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeveloperBoard,
                        contentDescription = "CAN Bus Monitor",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Vehicle Cluster Cockpit (Speed, Battery & TPMS)
                IconButton(
                    onClick = onOpenCockpit,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricCyan.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Vehicle Cockpit",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Floating HUD Overlay Toggle (Speed & Signals over CarPlay)
                if (onToggleHudOverlay != null) {
                    IconButton(
                        onClick = onToggleHudOverlay,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (config.showHudOverlay) ElectricCyan.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, if (config.showHudOverlay) ElectricCyan else Color.Transparent, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipToFront,
                            contentDescription = "HUD Overlay",
                            tint = if (config.showHudOverlay) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // System & Error Log File Viewer
                IconButton(
                    onClick = onOpenLogViewer,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Log File & Errors",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Diagnostics Export
                IconButton(
                    onClick = onOpenDiagnostics,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Diagnostics",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Reconnect button
                IconButton(
                    onClick = onReconnect,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reconnect",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
