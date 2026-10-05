package com.example.model

enum class SessionState(val label: String) {
    DISCONNECTED("Disconnected"),
    SCANNING("Scanning Network / USB…"),
    DISCOVERED("Phone Discovered"),
    AUTHENTICATING("AirPlay Authenticating…"),
    STREAMING("CarPlay Live Streaming"),
    RECONNECTING("Reconnecting Stream…"),
    FAILED("Connection Failed")
}

enum class TurnIcon {
    STRAIGHT,
    TURN_LEFT,
    TURN_RIGHT,
    SLIGHT_LEFT,
    SLIGHT_RIGHT,
    U_TURN,
    ROUNDABOUT,
    DESTINATION
}

data class NavigationTurn(
    val icon: TurnIcon = TurnIcon.TURN_RIGHT,
    val distanceText: String = "350 m",
    val streetName: String = "Guangzhou Avenue South",
    val nextManeuver: String = "Then take right fork onto Airport Express",
    val remainingDistanceKm: Float = 14.8f,
    val remainingTimeMin: Int = 19,
    val etaText: String = "22:24",
    val speedLimitKmh: Int = 80,
    val laneGuidance: List<Boolean> = listOf(false, true, true, false) // 4 lanes, middle 2 active
)

data class MediaMetadata(
    val title: String = "Midnight City",
    val artist: String = "M83",
    val album: String = "Hurry Up, We're Dreaming",
    val durationSeconds: Int = 243,
    val positionSeconds: Int = 88,
    val isPlaying: Boolean = true,
    val albumArtSeed: Int = 42,
    // 5-second dashboard song-on-change window
    val popoverRemainingSec: Int = 5,
    val isPopoverActive: Boolean = true
)

data class StreamMetrics(
    val fps: Float = 59.8f,
    val bitrateMbps: Float = 8.6f,
    val latencyMs: Int = 22,
    val packetLossPercent: Float = 0.01f,
    val audioBufferDepthMs: Int = 78,
    val framesDecoded: Long = 18450L,
    val droppedFrames: Long = 2L,
    val resolutionFormatted: String = "1920x1080 @ 60fps"
)

data class PhoneInfo(
    val name: String = "Han's iPhone",
    val model: String = "iPhone 15 Pro",
    val osVersion: String = "iOS 18.1",
    val ipAddress: String = "172.20.10.3",
    val macAddress: String = "a0:cd:f3:69:ef:4a",
    val interfaceType: String = "Existing Wi-Fi / Same LAN"
)

data class CarPlaySession(
    val state: SessionState = SessionState.STREAMING,
    val phoneInfo: PhoneInfo = PhoneInfo(),
    val metrics: StreamMetrics = StreamMetrics(),
    val navigation: NavigationTurn = NavigationTurn(),
    val media: MediaMetadata = MediaMetadata(),
    val lastError: String? = null,
    val sessionStartTimeMs: Long = System.currentTimeMillis()
)
