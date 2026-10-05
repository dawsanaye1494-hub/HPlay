package com.example.model

enum class ConnectionMode(val title: String, val description: String) {
    WIFI_DIRECT("Wi-Fi Direct (P2P)", "Lowest latency direct connection, no router needed"),
    CAR_HOTSPOT("Car Hotspot", "Uses vehicle Wi-Fi AP with stable interface monitoring"),
    SAME_LAN("Same LAN / Wi-Fi", "Scoped 172.20.10.x discovery with network-change cleanup"),
    WIRED_USB("Wired USB", "Direct USB cable with Apple VID 0x05AC & controller lease")
}

enum class AppearanceMode(val label: String) {
    SYSTEM("Follow System"),
    LIGHT_SENSOR("Car Light Sensor (CAN)"),
    DAY("Always Day"),
    NIGHT("Always Night")
}

enum class AuthMode(val label: String, val detail: String) {
    LOCAL("Local Software", "On-device software cryptographic pairing"),
    USB_CH341("USB-CH341 Peripheral", "Hardware dongle / serial secure authentication")
}

enum class AudioBufferMode(val label: String, val latencyMs: Int) {
    LOW_LATENCY("Low-Latency (80 ms)", 80),
    BALANCED("Balanced (150 ms)", 150),
    HIGH_STABILITY("High Stability (300 ms)", 300)
}

enum class SplitWidget(val label: String) {
    VEHICLE_CAN("EV Telemetry & CAN"),
    NAVIGATION_HUD("Cluster HUD Nav"),
    MEDIA_NOW_PLAYING("Media & Sync"),
    DRIVE_ASSIST("Drive Controls & Joystick")
}

data class CarPlayConfig(
    val connectionMode: ConnectionMode = ConnectionMode.SAME_LAN,
    // Custom integer resolution scaling from 30% to 160%
    val resolutionPercent: Int = 100, // 30 to 160
    val frameRate: Int = 60, // 30 or 60 fps
    val appearanceMode: AppearanceMode = AppearanceMode.NIGHT,
    val authMode: AuthMode = AuthMode.LOCAL,
    val audioBufferMode: AudioBufferMode = AudioBufferMode.LOW_LATENCY,

    // Live main-video picture controls
    val brightness: Float = 0f,      // -50 to +50
    val contrast: Float = 1.0f,     // 0.5 to 1.5
    val saturation: Float = 1.0f,   // 0.0 to 2.0
    val tint: Float = 0f,           // -30 to +30

    // Automotive Controls
    val wheelMapZoom: Boolean = true,
    val mainScreenJoystick: Boolean = false,
    val steeringCallEnabled: Boolean = true,
    val splitScreenEnabled: Boolean = true,
    val selectedSplitWidget: SplitWidget = SplitWidget.VEHICLE_CAN,
    val systemBarImmersive: Boolean = true,
    val showHudOverlay: Boolean = true,
    val hudOverlayCompact: Boolean = false,
    val hotspotAdbFallback: Boolean = true,
    val targetSubnet: String = "172.20.10.",
    val preferredChannel: Int = 36
) {
    companion object {
        const val MIN_RESOLUTION_PERCENT = 30
        const val MAX_RESOLUTION_PERCENT = 160

        fun calculateResolution(baseWidth: Int = 1920, baseHeight: Int = 1080, percent: Int): Pair<Int, Int> {
            val clamped = percent.coerceIn(MIN_RESOLUTION_PERCENT, MAX_RESOLUTION_PERCENT)
            val w = ((baseWidth * clamped / 100) / 16) * 16
            val h = ((baseHeight * clamped / 100) / 16) * 16
            return Pair(w, h)
        }
    }
}
