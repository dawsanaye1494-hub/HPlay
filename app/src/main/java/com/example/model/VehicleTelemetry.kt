package com.example.model

enum class GearPosition {
    P, R, N, D
}

data class TireData(
    val pressureBar: Float = 2.4f,
    val tempC: Int = 28,
    val isWarning: Boolean = false
) {
    val pressurePsi: Int
        get() = (pressureBar * 14.5038f).toInt()
}

data class TpmsStatus(
    val frontLeft: TireData = TireData(2.4f, 28, false),
    val frontRight: TireData = TireData(2.5f, 29, false),
    val rearLeft: TireData = TireData(2.4f, 27, false),
    val rearRight: TireData = TireData(2.4f, 28, false),
    val systemNormal: Boolean = true
)

enum class AirVentMode(val label: String) {
    FACE("Face"),
    FEET("Feet"),
    WINDSHIELD("Defrost"),
    FACE_AND_FEET("Bi-Level")
}

data class ClimateTelemetry(
    val driverTempC: Float = 22.0f,
    val passengerTempC: Float = 22.0f,
    val fanSpeed: Int = 3, // 1 to 7
    val isAcOn: Boolean = true,
    val isAutoMode: Boolean = true,
    val isRecirculation: Boolean = false,
    val isFrontDefrostOn: Boolean = false,
    val isRearDefrostOn: Boolean = false,
    val ventMode: AirVentMode = AirVentMode.FACE_AND_FEET,
    val seatHeatingDriver: Int = 1, // 0 to 3
    val seatHeatingPassenger: Int = 0, // 0 to 3
    val syncDualZone: Boolean = true,
    val canFrameHex: String = "0x3B0 [8] 2C 2C 31 83 00 00 00 00"
)

data class VehicleTelemetry(
    val vehicleModel: String = "GAC Hycan Z03",
    val speedKmh: Float = 0f,
    val motorRpm: Int = 0,
    val batterySocPercent: Int = 82,
    val estimatedRangeKm: Int = 418,
    val powerKw: Float = -2.1f, // Negative for regen, positive for discharge
    val batteryVoltageV: Float = 394.5f,
    val batteryCurrentA: Float = -5.3f,
    val batteryTempC: Float = 26.5f,
    val tpms: TpmsStatus = TpmsStatus(),
    val climate: ClimateTelemetry = ClimateTelemetry(),
    val gear: GearPosition = GearPosition.P,
    val isHandbrakeEngaged: Boolean = true,
    val isParked: Boolean = true,
    val parkedVideoLockActive: Boolean = false, // True if moving and video disabled by safety lock
    val lightSensorLux: Float = 145f,
    val isHeadlightOn: Boolean = false,
    val isLeftTurnSignalOn: Boolean = false,
    val isRightTurnSignalOn: Boolean = false,
    val isHazardLightOn: Boolean = false,
    val speedLimitKmh: Int = 80,
    val steeringAngleDeg: Float = 0f,
    val odoKm: Long = 18450,
    val driveMode: String = "ECO",
    val canBusActive: Boolean = true,
    val serialPort: String = "/dev/ttyS1 (500 kbps)",
    val adbDriverActive: Boolean = true
)

data class WheelControlEvent(
    val actionName: String,
    val keyCode: Int,
    val isPressed: Boolean,
    val timestampMs: Long = System.currentTimeMillis()
)
