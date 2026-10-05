package com.example.ui

import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.GearPosition
import com.example.ui.components.AutomotiveBottomDock
import com.example.ui.components.AutomotiveSplitWidgets
import com.example.ui.components.AutomotiveTopBar
import com.example.ui.components.CarPlayDisplayView
import com.example.viewmodel.ActiveDialog
import com.example.ui.dialogs.CanBusMonitorDialog
import com.example.ui.dialogs.DiagnosticsDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.viewmodel.HPlayViewModel

@Composable
fun HPlayMainScreen(
    viewModel: HPlayViewModel,
    modifier: Modifier = Modifier
) {
    val config by viewModel.config.collectAsState()
    val session by viewModel.session.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val canFrames by viewModel.canRawFrames.collectAsState()
    val activeDialog by viewModel.activeDialog.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val exportedFile by viewModel.exportedFile.collectAsState()
    val isNightMode by viewModel.isNightMode.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("hplay_main_screen"),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Automotive Status Bar (Can be hidden if full-bleed immersive)
            AnimatedVisibility(
                visible = !config.systemBarImmersive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AutomotiveTopBar(
                    telemetry = telemetry,
                    isImmersive = config.systemBarImmersive,
                    isNightMode = isNightMode,
                    onToggleNightMode = { viewModel.toggleNightMode() },
                    onToggleImmersive = { viewModel.toggleImmersiveBars() },
                    onOpenDashboard = { viewModel.openDialog(com.example.viewmodel.ActiveDialog.HIGH_CONTRAST_DASHBOARD) }
                )
            }

            // Main Stage: Split Screen or Fullscreen CarPlay (Adaptive Landscape & Portrait)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val isWide = maxWidth >= 680.dp
                if (isWide) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Left/Main Area: Interactive CarPlay Display
                        CarPlayDisplayView(
                            session = session,
                            config = config,
                            telemetry = telemetry,
                            onWheelZoom = { zoomIn -> viewModel.onWheelZoom(zoomIn) },
                            onPlayPause = { viewModel.togglePlayPause() },
                            onNext = { viewModel.nextTrack() },
                            onPrev = { viewModel.previousTrack() },
                            modifier = Modifier
                                .weight(if (config.splitScreenEnabled) 0.68f else 1.0f)
                        )

                        // Right Area: Secondary Automotive Status Widgets (Split screen mode)
                        if (config.splitScreenEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AutomotiveSplitWidgets(
                                config = config,
                                session = session,
                                telemetry = telemetry,
                                onSelectWidget = { viewModel.setSplitWidget(it) },
                                onWheelButton = { action, code -> viewModel.onWheelButton(action, code) },
                                onSetGear = { gear -> viewModel.setGear(gear) },
                                onPlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.nextTrack() },
                                onPrev = { viewModel.previousTrack() },
                                modifier = Modifier.weight(0.32f)
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        CarPlayDisplayView(
                            session = session,
                            config = config,
                            telemetry = telemetry,
                            onWheelZoom = { zoomIn -> viewModel.onWheelZoom(zoomIn) },
                            onPlayPause = { viewModel.togglePlayPause() },
                            onNext = { viewModel.nextTrack() },
                            onPrev = { viewModel.previousTrack() },
                            modifier = Modifier.weight(if (config.splitScreenEnabled) 0.58f else 1.0f)
                        )
                        if (config.splitScreenEnabled) {
                            Spacer(modifier = Modifier.height(6.dp))
                            AutomotiveSplitWidgets(
                                config = config,
                                session = session,
                                telemetry = telemetry,
                                onSelectWidget = { viewModel.setSplitWidget(it) },
                                onWheelButton = { action, code -> viewModel.onWheelButton(action, code) },
                                onSetGear = { gear -> viewModel.setGear(gear) },
                                onPlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.nextTrack() },
                                onPrev = { viewModel.previousTrack() },
                                modifier = Modifier.weight(0.42f)
                            )
                        }
                    }
                }

                // Floating HUD Overlay Component (Keeps Speed & Turn Signals Visible Over CarPlay)
                if (config.showHudOverlay) {
                    com.example.ui.components.AutomotiveHudOverlay(
                        telemetry = telemetry,
                        isCompact = config.hudOverlayCompact,
                        onToggleCompact = { viewModel.toggleHudCompact() },
                        onToggleLeftSignal = { viewModel.toggleLeftTurnSignal() },
                        onToggleRightSignal = { viewModel.toggleRightTurnSignal() },
                        onToggleHazard = { viewModel.toggleHazardLight() },
                        onClose = { viewModel.toggleHudOverlay() },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 12.dp, top = 8.dp)
                    )
                }
            }

            // Automotive Bottom Dock
            AutomotiveBottomDock(
                session = session,
                config = config,
                onSelectMode = { viewModel.setConnectionMode(it) },
                onToggleSplit = { viewModel.toggleSplitScreen() },
                onOpenSettings = { viewModel.openDialog(com.example.viewmodel.ActiveDialog.SETTINGS) },
                onOpenCanMonitor = { viewModel.openDialog(com.example.viewmodel.ActiveDialog.CAN_BUS_MONITOR) },
                onOpenDiagnostics = { viewModel.openDialog(com.example.viewmodel.ActiveDialog.DIAGNOSTICS) },
                onOpenCockpit = { viewModel.openDialog(com.example.viewmodel.ActiveDialog.HIGH_CONTRAST_DASHBOARD) },
                onOpenLogViewer = { viewModel.openDialog(com.example.viewmodel.ActiveDialog.LOG_VIEWER) },
                onToggleHudOverlay = { viewModel.toggleHudOverlay() },
                onReconnect = { viewModel.reconnect() },
                onDisconnect = { viewModel.disconnect() }
            )
        }

        // Active Overlays / Dialogs
        when (activeDialog) {
            com.example.viewmodel.ActiveDialog.SETTINGS -> {
                SettingsDialog(
                    initialConfig = config,
                    onSaveConfig = { updated -> viewModel.updateConfig(updated) },
                    onDismiss = { viewModel.closeDialog() }
                )
            }
            com.example.viewmodel.ActiveDialog.CAN_BUS_MONITOR -> {
                CanBusMonitorDialog(
                    telemetry = telemetry,
                    rawFrames = canFrames,
                    onSetGear = { viewModel.setGear(it) },
                    onDismiss = { viewModel.closeDialog() }
                )
            }
            com.example.viewmodel.ActiveDialog.DIAGNOSTICS -> {
                DiagnosticsDialog(
                    logs = recentLogs,
                    exportedFile = exportedFile,
                    onExportToDownloads = { ctx, onShare ->
                        viewModel.exportDiagnostics(ctx, onShare)
                    },
                    onDismiss = { viewModel.closeDialog() }
                )
            }
            com.example.viewmodel.ActiveDialog.VEHICLE_CLUSTER_COCKPIT -> {
                androidx.compose.ui.window.Dialog(onDismissRequest = { viewModel.closeDialog() }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(340.dp)
                    ) {
                        com.example.ui.components.VehicleClusterCockpitView(
                            telemetry = telemetry,
                            onSetGear = { viewModel.setGear(it) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            com.example.viewmodel.ActiveDialog.HIGH_CONTRAST_DASHBOARD -> {
                androidx.compose.ui.window.Dialog(
                    onDismissRequest = { viewModel.closeDialog() },
                    properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                ) {
                    com.example.ui.screens.DriverDashboardScreen(
                        telemetry = telemetry,
                        isNightMode = isNightMode,
                        appearanceMode = config.appearanceMode,
                        onToggleNightMode = { viewModel.toggleNightMode() },
                        onSetAppearanceMode = { viewModel.setAppearanceMode(it) },
                        onSetGear = { viewModel.setGear(it) },
                        onAdjustClimateTemp = { viewModel.adjustClimateTemp(it) },
                        onSetFanSpeed = { viewModel.setFanSpeed(it) },
                        onToggleAc = { viewModel.toggleAc() },
                        onToggleAutoClimate = { viewModel.toggleAutoClimate() },
                        onToggleRecirculation = { viewModel.toggleRecirculation() },
                        onToggleFrontDefrost = { viewModel.toggleFrontDefrost() },
                        onToggleRearDefrost = { viewModel.toggleRearDefrost() },
                        onCycleVentMode = { viewModel.cycleVentMode() },
                        onCycleSeatHeating = { viewModel.cycleSeatHeating() },
                        onSetDriveMode = { viewModel.setDriveMode(it) },
                        onBack = { viewModel.closeDialog() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            com.example.viewmodel.ActiveDialog.LOG_VIEWER -> {
                com.example.ui.dialogs.LogViewerDialog(
                    telemetry = telemetry,
                    onDismiss = { viewModel.closeDialog() }
                )
            }
            else -> { /* No Dialog */ }
        }
    }
}
