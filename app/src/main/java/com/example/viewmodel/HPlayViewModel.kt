package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.carplay.CarPlayReceiverEngine
import com.example.driver.CanBusDriver
import com.example.model.AppearanceMode
import com.example.model.CarPlayConfig
import com.example.model.CarPlaySession
import com.example.model.ConnectionMode
import com.example.model.DiagnosticEntry
import com.example.model.DiagnosticReport
import com.example.model.GearPosition
import com.example.model.SplitWidget
import com.example.model.VehicleTelemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ActiveDialog {
    NONE,
    SETTINGS,
    CAN_BUS_MONITOR,
    DIAGNOSTICS,
    RESOLUTION_PREVIEW,
    VEHICLE_CLUSTER_COCKPIT,
    HIGH_CONTRAST_DASHBOARD,
    LOG_VIEWER
}

class HPlayViewModel(application: Application) : AndroidViewModel(application) {

    val engine = CarPlayReceiverEngine(application.applicationContext, viewModelScope)
    val canDriver = CanBusDriver(application.applicationContext, viewModelScope)

    val config: StateFlow<CarPlayConfig> = engine.config
    val session: StateFlow<CarPlaySession> = engine.session
    val telemetry: StateFlow<VehicleTelemetry> = canDriver.telemetry
    val canRawFrames: StateFlow<List<String>> = canDriver.canRawFrames

    private val _activeDialog = MutableStateFlow(ActiveDialog.NONE)
    val activeDialog: StateFlow<ActiveDialog> = _activeDialog.asStateFlow()

    private val _recentLogs = MutableStateFlow<List<DiagnosticEntry>>(emptyList())
    val recentLogs: StateFlow<List<DiagnosticEntry>> = _recentLogs.asStateFlow()

    private val _exportedFile = MutableStateFlow<File?>(null)
    val exportedFile: StateFlow<File?> = _exportedFile.asStateFlow()

    // Determine current effective appearance (Day vs Night)
    val isNightMode: StateFlow<Boolean> = combine(config, telemetry) { conf, telem ->
        when (conf.appearanceMode) {
            AppearanceMode.DAY -> false
            AppearanceMode.NIGHT -> true
            AppearanceMode.LIGHT_SENSOR -> telem.lightSensorLux < 100f || telem.isHeadlightOn
            AppearanceMode.SYSTEM -> true // Automotive default
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    init {
        // Collect engine diagnostic logs into report log
        viewModelScope.launch {
            val list = mutableListOf<DiagnosticEntry>()
            engine.diagnosticLogs.collect { msg ->
                val time = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
                list.add(0, DiagnosticEntry(time, "ENGINE", "INFO", msg))
                if (list.size > 80) list.removeAt(list.size - 1)
                _recentLogs.value = list.toList()
            }
        }
    }

    fun openDialog(dialog: ActiveDialog) {
        _activeDialog.value = dialog
    }

    fun closeDialog() {
        _activeDialog.value = ActiveDialog.NONE
    }

    fun updateConfig(newConfig: CarPlayConfig) {
        engine.updateConfig(newConfig)
    }

    fun setConnectionMode(mode: ConnectionMode) {
        engine.updateConfig(config.value.copy(connectionMode = mode))
    }

    fun setResolutionPercent(percent: Int) {
        engine.updateConfig(config.value.copy(resolutionPercent = percent.coerceIn(CarPlayConfig.MIN_RESOLUTION_PERCENT, CarPlayConfig.MAX_RESOLUTION_PERCENT)))
    }

    fun setSplitWidget(widget: SplitWidget) {
        engine.updateConfig(config.value.copy(selectedSplitWidget = widget))
    }

    fun toggleSplitScreen() {
        engine.updateConfig(config.value.copy(splitScreenEnabled = !config.value.splitScreenEnabled))
    }

    fun toggleImmersiveBars() {
        engine.updateConfig(config.value.copy(systemBarImmersive = !config.value.systemBarImmersive))
    }

    fun setGear(gear: GearPosition) {
        canDriver.setGear(gear)
    }

    fun onWheelZoom(zoomIn: Boolean) {
        engine.dispatchWheelZoom(zoomIn)
    }

    fun adjustClimateTemp(delta: Float) = canDriver.adjustClimateTemp(delta)
    fun setFanSpeed(speed: Int) = canDriver.setFanSpeed(speed)
    fun toggleAc() = canDriver.toggleAc()
    fun toggleAutoClimate() = canDriver.toggleAutoClimate()
    fun toggleRecirculation() = canDriver.toggleRecirculation()
    fun toggleFrontDefrost() = canDriver.toggleFrontDefrost()
    fun toggleRearDefrost() = canDriver.toggleRearDefrost()
    fun cycleVentMode() = canDriver.cycleVentMode()
    fun cycleSeatHeating() = canDriver.cycleSeatHeating()
    fun setDriveMode(mode: String) = canDriver.setDriveMode(mode)

    fun setAppearanceMode(mode: com.example.model.AppearanceMode) {
        val updated = config.value.copy(appearanceMode = mode)
        updateConfig(updated)
    }

    fun toggleNightMode() {
        val next = when (config.value.appearanceMode) {
            com.example.model.AppearanceMode.NIGHT -> com.example.model.AppearanceMode.DAY
            com.example.model.AppearanceMode.DAY -> com.example.model.AppearanceMode.LIGHT_SENSOR
            com.example.model.AppearanceMode.LIGHT_SENSOR -> com.example.model.AppearanceMode.NIGHT
            com.example.model.AppearanceMode.SYSTEM -> com.example.model.AppearanceMode.NIGHT
        }
        setAppearanceMode(next)
    }

    fun onWheelButton(action: String, code: Int) {
        canDriver.dispatchWheelButton(action, code, true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            canDriver.dispatchWheelButton(action, code, false)
        }
    }

    fun togglePlayPause() {
        engine.togglePlayPause()
    }

    fun toggleLeftTurnSignal() = canDriver.toggleLeftTurnSignal()
    fun toggleRightTurnSignal() = canDriver.toggleRightTurnSignal()
    fun toggleHazardLight() = canDriver.toggleHazardLight()

    fun toggleHudOverlay() {
        val current = config.value
        updateConfig(current.copy(showHudOverlay = !current.showHudOverlay))
    }

    fun toggleHudCompact() {
        val current = config.value
        updateConfig(current.copy(hudOverlayCompact = !current.hudOverlayCompact))
    }

    fun nextTrack() {
        engine.nextTrack()
    }

    fun previousTrack() {
        engine.previousTrack()
    }

    fun reconnect() {
        engine.startSession()
    }

    fun disconnect() {
        engine.disconnect()
    }

    fun dispatchTouchEvent(event: MotionEvent, w: Float, h: Float): Boolean {
        return engine.dispatchTouchEvent(event, w, h)
    }

    fun dispatchHardwareKey(event: KeyEvent): Boolean {
        return engine.dispatchHardwareKey(event)
    }

    fun exportDiagnostics(context: Context, onReadyToShare: (Intent) -> Unit) {
        val report = DiagnosticReport(
            connectionMode = config.value.connectionMode.title,
            phoneIp = session.value.phoneInfo.ipAddress,
            headUnitIp = "172.20.10.3",
            canBusPort = telemetry.value.serialPort,
            canPacketsReceived = 52310L,
            averageFps = session.value.metrics.fps,
            averageLatencyMs = session.value.metrics.latencyMs,
            audioBufferMode = config.value.audioBufferMode.label,
            entries = if (_recentLogs.value.isNotEmpty()) _recentLogs.value else DiagnosticReport().entries
        )
        try {
            val file = report.exportToFile(context)
            _exportedFile.value = file
            Toast.makeText(context, "Saved to ${file.name}", Toast.LENGTH_LONG).show()
            val shareIntent = report.createShareIntent(context, file)
            onReadyToShare(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine.unregister()
        canDriver.stop()
    }
}
