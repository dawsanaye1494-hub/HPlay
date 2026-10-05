package com.example.model

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DiagnosticEntry(
    val timestamp: String,
    val tag: String,
    val level: String,
    val message: String
)

data class DiagnosticReport(
    val deviceModel: String = "GAC Hycan Z03 (G6SA-r8a7796)",
    val androidVersion: String = "Android 8.1.0 (API 27)",
    val kernel: String = "4.14.86+",
    val ramTotal: String = "7.23 GB",
    val resolution: String = "1920x1080 (160dpi)",
    val connectionMode: String = "Same LAN / Existing Wi-Fi",
    val phoneIp: String = "172.20.10.3",
    val headUnitIp: String = "172.20.10.2",
    val canBusPort: String = "/dev/ttyS1 (500k baud)",
    val canPacketsReceived: Long = 48210L,
    val averageFps: Float = 59.8f,
    val averageLatencyMs: Int = 22,
    val audioBufferMode: String = "Low-Latency (80 ms)",
    val entries: List<DiagnosticEntry> = listOf(
        DiagnosticEntry("22:04:11.102", "NETWORK", "INFO", "Wi-Fi interface up: wlan0 (172.20.10.2/28)"),
        DiagnosticEntry("22:04:11.450", "DISCOVERY", "INFO", "Target scoped discovery on 172.20.10.*. Found Han's iPhone (172.20.10.3)"),
        DiagnosticEntry("22:04:12.010", "AIRPLAY", "INFO", "RTSP SETUP session initialized, pairing protocol=HomeKit-SRP, auth=LOCAL"),
        DiagnosticEntry("22:04:12.890", "MEDIACODEC", "INFO", "Configured H.264 OMX.renesas.video.decoder for 1920x1080@60fps (Hardware Accel)"),
        DiagnosticEntry("22:04:13.200", "AUDIO", "INFO", "AudioTrack low-latency media buffer opened: 48000Hz stereo 16-bit PCM"),
        DiagnosticEntry("22:04:13.410", "CANBUS", "INFO", "CAN driver attached to GAC Hycan Z03 bus: Speed=0km/h SoC=82% Gear=P"),
        DiagnosticEntry("22:04:15.820", "CARPLAY", "INFO", "Main screen stream running smoothly: latency=22ms, jitter=1.2ms")
    )
) {
    fun toFormattedText(): String {
        val sb = StringBuilder()
        sb.append("========================================\n")
        sb.append("   H PLAY AUTOMOTIVE DIAGNOSTIC REPORT   \n")
        sb.append("========================================\n")
        sb.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
        sb.append("Vehicle / Head Unit: $deviceModel\n")
        sb.append("OS Version: $androidVersion | Kernel: $kernel\n")
        sb.append("Display: $resolution | Total RAM: $ramTotal\n")
        sb.append("Connection: $connectionMode (Phone: $phoneIp, HU: $headUnitIp)\n")
        sb.append("CAN Bus Interface: $canBusPort | Frames: $canPacketsReceived\n")
        sb.append("Streaming Perf: Avg FPS=$averageFps | Latency=${averageLatencyMs}ms | AudioBuffer=$audioBufferMode\n\n")
        sb.append("--- EVENT LOG ---\n")
        for (entry in entries) {
            sb.append("[${entry.timestamp}] [${entry.level}] [${entry.tag}] ${entry.message}\n")
        }
        sb.append("========================================\n")
        sb.append("Local report generated securely on head unit. No data is sent to external servers.\n")
        return sb.toString()
    }

    fun exportToFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "HPlay_Diagnostic_$timeStamp.txt"

        // Try primary Downloads dir, then app external files dir, then cache
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val targetFile: File = if (downloadsDir != null && downloadsDir.exists() && downloadsDir.canWrite()) {
            File(downloadsDir, fileName)
        } else {
            val appExt = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (appExt != null && appExt.exists()) {
                File(appExt, fileName)
            } else {
                File(context.cacheDir, fileName)
            }
        }

        targetFile.writeText(toFormattedText())
        return targetFile
    }

    fun createShareIntent(context: Context, file: File): Intent {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "H Play Diagnostic Report - GAC Hycan Z03")
            putExtra(Intent.EXTRA_TEXT, toFormattedText())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, "Export Diagnostic Report")
    }
}
