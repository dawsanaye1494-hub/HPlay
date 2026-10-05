package com.example.logger

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

enum class LogLevel(val label: String) {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR"),
    CRASH("CRASH")
}

data class LogEntry(
    val id: Long = System.nanoTime(),
    val timestamp: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val throwableStackTrace: String? = null
) {
    fun toLogLine(): String {
        val base = "[$timestamp] [${level.label}] [$tag] $message"
        return if (throwableStackTrace != null) {
            "$base\n$throwableStackTrace"
        } else {
            base
        }
    }
}

/**
 * High-performance, persistent file logger designed for Android 8.1 automotive head units.
 * Writes rolling logs to device storage (hplay_system.log and hplay_crash.log)
 * and captures uncaught crashes, CAN errors, USB host disconnects, and MediaCodec events.
 */
object FileLogManager {
    private const val TAG = "HPlay_Logger"
    private const val MAX_MEMORY_LOGS = 300
    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024 // 5 MB rolling cap

    private val logQueue = ConcurrentLinkedQueue<LogEntry>()
    private val _logsFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    val logsFlow: StateFlow<List<LogEntry>> = _logsFlow.asStateFlow()

    private var logDir: File? = null
    private var currentLogFile: File? = null
    private var crashLogFile: File? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private var isInitialized = false

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)
    private val shortTimeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        val appContext = context.applicationContext

        // Set up log directory in app-specific external storage or internal storage
        val extDir = appContext.getExternalFilesDir("logs")
        logDir = if (extDir != null && extDir.exists()) {
            extDir
        } else {
            File(appContext.filesDir, "logs").apply { mkdirs() }
        }

        currentLogFile = File(logDir, "hplay_system.log")
        crashLogFile = File(logDir, "hplay_crash.log")

        // Write header if new file
        if (currentLogFile?.exists() != true) {
            writeHeader()
        }

        // Global Uncaught Exception Handler to capture fatal errors/crashes
        setupCrashHandler()

        // Log system boot info
        i("SYSTEM", "H Play File Logger initialized on Android 8.1 (GAC Hycan Z03)")
        i("DEVICE", "Board=${Build.BOARD}, Hardware=${Build.HARDWARE}, Model=${Build.MODEL}, API=${Build.VERSION.SDK_INT}")
        i("STORAGE", "Log file active at: ${currentLogFile?.absolutePath}")
    }

    private fun writeHeader() {
        try {
            currentLogFile?.let { file ->
                val header = buildString {
                    append("====================================================\n")
                    append("   H PLAY AUTOMOTIVE SYSTEM & ERROR LOG FILE       \n")
                    append("====================================================\n")
                    append("Started: ${timeFormat.format(Date())}\n")
                    append("OS: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
                    append("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})\n")
                    append("Architecture: ${Build.SUPPORTED_ABIS.joinToString()}\n")
                    append("====================================================\n\n")
                }
                file.writeText(header)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed writing header: ${e.message}")
        }
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()

                val crashLog = buildString {
                    append("\n\n!!!!!!!!!!!!!!!! FATAL CRASH DETECTED !!!!!!!!!!!!!!!!\n")
                    append("Timestamp: ${timeFormat.format(Date())}\n")
                    append("Thread: ${thread.name} (id=${thread.id})\n")
                    append("Exception: ${throwable.javaClass.name}: ${throwable.message}\n")
                    append("Stack Trace:\n$stackTrace\n")
                    append("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n")
                }

                // Write to both crash file and current system log
                crashLogFile?.appendText(crashLog)
                currentLogFile?.appendText(crashLog)

                // Log into memory
                log(LogLevel.CRASH, "CRASH_HANDLER", "Fatal exception on thread ${thread.name}: ${throwable.message}", stackTrace)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving crash log: ${e.message}")
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun d(tag: String, msg: String) = log(LogLevel.DEBUG, tag, msg)
    fun i(tag: String, msg: String) = log(LogLevel.INFO, tag, msg)
    fun w(tag: String, msg: String, tr: Throwable? = null) = log(LogLevel.WARN, tag, msg, tr?.let { getStackTraceString(it) })
    fun e(tag: String, msg: String, tr: Throwable? = null) = log(LogLevel.ERROR, tag, msg, tr?.let { getStackTraceString(it) })

    fun log(level: LogLevel, tag: String, message: String, stackTrace: String? = null) {
        val now = Date()
        val entry = LogEntry(
            timestamp = shortTimeFormat.format(now),
            level = level,
            tag = tag,
            message = message,
            throwableStackTrace = stackTrace
        )

        // Android logcat mirroring
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARN -> Log.w(tag, message)
            LogLevel.ERROR, LogLevel.CRASH -> Log.e(tag, message)
        }

        // Memory buffer for UI
        logQueue.add(entry)
        while (logQueue.size > MAX_MEMORY_LOGS) {
            logQueue.poll()
        }
        _logsFlow.value = logQueue.toList()

        // Disk write in background
        scope.launch {
            writeToFile(entry)
        }
    }

    private fun writeToFile(entry: LogEntry) {
        try {
            val file = currentLogFile ?: return
            // Check rolling size cap
            if (file.exists() && file.length() > MAX_FILE_SIZE_BYTES) {
                val backup = File(file.parentFile, "hplay_system_old.log")
                if (backup.exists()) backup.delete()
                file.renameTo(backup)
                writeHeader()
            }

            FileWriter(file, true).use { writer ->
                writer.append(entry.toLogLine()).append("\n")
            }
        } catch (e: Exception) {
            Log.e(TAG, "File log write error: ${e.message}")
        }
    }

    fun getLogFilePath(): String {
        return currentLogFile?.absolutePath ?: "Unavailable"
    }

    fun getLogFileSizeFormatted(): String {
        val bytes = currentLogFile?.length() ?: 0L
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format(Locale.US, "%.2f MB", bytes / (1024f * 1024f))
        }
    }

    fun clearLogs() {
        logQueue.clear()
        _logsFlow.value = emptyList()
        scope.launch {
            try {
                currentLogFile?.delete()
                writeHeader()
                i("LOGGER", "Logs cleared by user")
            } catch (e: Exception) {
                Log.e(TAG, "Failed clearing log file: ${e.message}")
            }
        }
    }

    private var lastExportedFile: File? = null

    fun getLastExportedFile(): File? = lastExportedFile

    /**
     * Exports all system logs, fatal crashes, CAN bus telemetry, and hardware metrics
     * into a human-readable .txt file saved directly to the device's storage (e.g. Download/ or Documents/).
     */
    fun exportTxtLogFile(context: Context, telemetry: com.example.model.VehicleTelemetry? = null): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "HPlay_Bug_Log_$timeStamp.txt"

        // 1. Try public Download folder on device
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDir != null && !downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val targetFile = if (downloadsDir != null && downloadsDir.exists() && downloadsDir.canWrite()) {
            File(downloadsDir, fileName)
        } else {
            val extDocs = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (extDocs != null && !extDocs.exists()) extDocs.mkdirs()
            if (extDocs != null && extDocs.exists()) {
                File(extDocs, fileName)
            } else {
                File(context.cacheDir, fileName)
            }
        }

        val totalErrors = logQueue.count { it.level == LogLevel.ERROR || it.level == LogLevel.CRASH }
        val totalWarnings = logQueue.count { it.level == LogLevel.WARN }
        val totalCrashes = logQueue.count { it.level == LogLevel.CRASH }

        val txtContent = buildString {
            append("======================================================================\n")
            append("      H PLAY AUTOMOTIVE SYSTEM & BUG ERROR LOG REPORT (.TXT)          \n")
            append("======================================================================\n")
            append("Generated: ${timeFormat.format(Date())}\n")
            append("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.PRODUCT})\n")
            append("OS Platform: Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
            append("Board/CPU: ${Build.BOARD} (${Build.HARDWARE}) | Kernel: Linux ${System.getProperty("os.version")}\n")
            append("Saved Location on Device: ${targetFile.absolutePath}\n")
            append("Disk Log Source: ${currentLogFile?.absolutePath}\n\n")

            if (telemetry != null) {
                append("--- VEHICLE TELEMETRY & CAN BUS SNAPSHOT ---\n")
                append("Vehicle Model: ${telemetry.vehicleModel}\n")
                append("Speed: ${telemetry.speedKmh} km/h | Gear: ${telemetry.gear} | Motor RPM: ${telemetry.motorRpm}\n")
                append("Battery SoC: ${telemetry.batterySocPercent}% | Est Range: ${telemetry.estimatedRangeKm} km | Power: ${telemetry.powerKw} kW\n")
                append("Battery Pack: ${telemetry.batteryVoltageV} V | ${telemetry.batteryCurrentA} A | ${telemetry.batteryTempC} °C\n")
                append("Drive Mode: ${telemetry.driveMode} | EPB Handbrake: ${telemetry.isHandbrakeEngaged}\n")
                append("Ambient Light Sensor: ${telemetry.lightSensorLux} Lux | Headlights: ${telemetry.isHeadlightOn}\n")
                append("Climate: Target ${telemetry.climate.driverTempC}°C | Fan Speed: ${telemetry.climate.fanSpeed}/7 | AC: ${if (telemetry.climate.isAcOn) "ON" else "OFF"} | Auto: ${telemetry.climate.isAutoMode}\n")
                append("TPMS Tires: FL=${telemetry.tpms.frontLeft.pressureBar}bar, FR=${telemetry.tpms.frontRight.pressureBar}bar, RL=${telemetry.tpms.rearLeft.pressureBar}bar, RR=${telemetry.tpms.rearRight.pressureBar}bar\n")
                append("CAN Serial Port: ${telemetry.serialPort} | Active: ${telemetry.canBusActive}\n\n")
            }

            append("--- BUG & ERROR DIAGNOSTIC SUMMARY ---\n")
            append("Total Log Entries in Session: ${logQueue.size}\n")
            append("Fatal Crashes: $totalCrashes\n")
            append("Errors Detected: $totalErrors\n")
            append("Warnings Logged: $totalWarnings\n")
            append("Primary Status: ${if (totalErrors > 0) "ANOMALIES RECORDED - REVIEW BELOW" else "SYSTEM NORMAL"}\n\n")

            append("--- FULL CHRONOLOGICAL EVENT & ERROR LOG ENTRIES ---\n")
            if (currentLogFile?.exists() == true && currentLogFile?.length() ?: 0L > 0) {
                append(currentLogFile?.readText())
            } else {
                for (entry in logQueue) {
                    append(entry.toLogLine()).append("\n")
                }
            }
            append("\n======================================================================\n")
            append("End of Bug Report. Plain text format saved locally on device storage.\n")
        }

        targetFile.writeText(txtContent)
        lastExportedFile = targetFile

        // Log this export to the active log file
        i("LOGGER", "Exported bug & error report to .txt file: ${targetFile.absolutePath} (${targetFile.length()} bytes)")

        return targetFile
    }

    fun exportLogFile(context: Context): File {
        return exportTxtLogFile(context, null)
    }

    fun createShareTxtIntent(context: Context, file: File): android.content.Intent {
        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_SUBJECT, "H Play Bug & Error Report - ${file.name}")
            putExtra(android.content.Intent.EXTRA_TEXT, file.readText())

            try {
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                Log.w(TAG, "FileProvider uri error (fallback to text): ${e.message}")
            }
        }
        return android.content.Intent.createChooser(sendIntent, "Share Bug Log (.txt)")
    }

    fun simulateTestError() {
        try {
            throw java.io.IOException("CAN Bus timeout on /dev/ttyS1: frame 0x3B0 dropped after 1500ms")
        } catch (e: Exception) {
            e("CAN_DIAG", "Simulated driver anomaly captured for verification", e)
        }
    }

    private fun getStackTraceString(tr: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        tr.printStackTrace(pw)
        return sw.toString()
    }
}
