package com.example

import com.example.model.CarPlayConfig
import com.example.model.DiagnosticReport
import com.example.model.GearPosition
import com.example.model.VehicleTelemetry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testResolutionScalingLimitsAndAlignment() {
        // Shared limits: clamped between 30% and 160%
        val (w30, h30) = CarPlayConfig.calculateResolution(1920, 1080, 30)
        assertEquals(576, w30)
        assertEquals(320, h30)
        assertEquals(0, w30 % 16)
        assertEquals(0, h30 % 16)

        val (w100, h100) = CarPlayConfig.calculateResolution(1920, 1080, 100)
        assertEquals(1920, w100)
        assertEquals(1072, h100) // 16-pixel aligned

        val (w160, h160) = CarPlayConfig.calculateResolution(1920, 1080, 160)
        assertEquals(3072, w160)
        assertEquals(1728, h160)

        // Under 30% clamp
        val (wUnder, _) = CarPlayConfig.calculateResolution(1920, 1080, 10)
        assertEquals(576, wUnder)

        // Over 160% clamp
        val (wOver, _) = CarPlayConfig.calculateResolution(1920, 1080, 250)
        assertEquals(3072, wOver)
    }

    @Test
    fun testVehicleTelemetryDefaults() {
        val telemetry = VehicleTelemetry()
        assertEquals("GAC Hycan Z03", telemetry.vehicleModel)
        assertEquals(GearPosition.P, telemetry.gear)
        assertTrue(telemetry.isParked)
        assertTrue(telemetry.isHandbrakeEngaged)
        assertTrue(telemetry.batterySocPercent > 0)
        assertNotNull(telemetry.tpms)
        assertEquals(2.4f, telemetry.tpms.frontLeft.pressureBar, 0.01f)
        assertEquals(34, telemetry.tpms.frontLeft.pressurePsi)
        assertTrue(telemetry.tpms.systemNormal)
        assertNotNull(telemetry.climate)
        assertEquals(22.0f, telemetry.climate.driverTempC, 0.01f)
        assertTrue(telemetry.climate.isAcOn)
        assertTrue(telemetry.climate.isAutoMode)
        assertEquals(3, telemetry.climate.fanSpeed)
    }

    @Test
    fun testClimateTelemetryDefaults() {
        val telemetry = VehicleTelemetry()
        val climate = telemetry.climate
        assertEquals(22.0f, climate.driverTempC, 0.01f)
        assertTrue(climate.isAcOn)
        assertTrue(climate.isAutoMode)
        assertEquals(3, climate.fanSpeed)
        val updated = climate.copy(driverTempC = 23.5f, fanSpeed = 5, isAcOn = false)
        assertEquals(23.5f, updated.driverTempC, 0.01f)
        assertEquals(5, updated.fanSpeed)
        assertEquals(false, updated.isAcOn)
    }

    @Test
    fun testDiagnosticReportGeneration() {
        val report = DiagnosticReport()
        val text = report.toFormattedText()
        assertNotNull(text)
        assertTrue(text.contains("H PLAY AUTOMOTIVE DIAGNOSTIC REPORT"))
        assertTrue(text.contains("GAC Hycan Z03"))
        assertTrue(text.contains("172.20.10.3"))
    }

    @Test
    fun testFileLogManagerLogEntry() {
        val entry = com.example.logger.LogEntry(
            timestamp = "12:00:00.000",
            level = com.example.logger.LogLevel.ERROR,
            tag = "CAN_BUS",
            message = "Simulated parity check error",
            throwableStackTrace = "java.lang.Exception: CAN parity error"
        )
        val line = entry.toLogLine()
        assertTrue(line.contains("[12:00:00.000]"))
        assertTrue(line.contains("[ERROR]"))
        assertTrue(line.contains("[CAN_BUS]"))
        assertTrue(line.contains("Simulated parity check error"))
        assertTrue(line.contains("CAN parity error"))
    }

    @Test
    fun testNightModeAppearanceToggles() {
        val configNight = CarPlayConfig(appearanceMode = com.example.model.AppearanceMode.NIGHT)
        assertEquals(com.example.model.AppearanceMode.NIGHT, configNight.appearanceMode)

        val configDay = CarPlayConfig(appearanceMode = com.example.model.AppearanceMode.DAY)
        assertEquals(com.example.model.AppearanceMode.DAY, configDay.appearanceMode)

        val configSensor = CarPlayConfig(appearanceMode = com.example.model.AppearanceMode.LIGHT_SENSOR)
        assertEquals(com.example.model.AppearanceMode.LIGHT_SENSOR, configSensor.appearanceMode)
    }

    @Test
    fun testTxtBugLogFileGeneration() {
        val entry = com.example.logger.LogEntry(
            timestamp = "01:40:22.100",
            level = com.example.logger.LogLevel.ERROR,
            tag = "CAN_BUS",
            message = "Dropped frame 0x3B0 on /dev/ttyS1"
        )
        val telemetry = VehicleTelemetry()
        val formatted = buildString {
            append("H PLAY AUTOMOTIVE SYSTEM & BUG ERROR LOG REPORT (.TXT)\n")
            append("Vehicle Model: ${telemetry.vehicleModel}\n")
            append("Battery SoC: ${telemetry.batterySocPercent}%\n")
            append(entry.toLogLine())
        }
        assertTrue(formatted.contains(".TXT"))
        assertTrue(formatted.contains("GAC Hycan Z03"))
        assertTrue(formatted.contains("Battery SoC: 82%"))
        assertTrue(formatted.contains("Dropped frame 0x3B0"))
    }

    @Test
    fun testHudOverlayTelemetryAndTurnSignals() {
        val telemetry = VehicleTelemetry(
            speedKmh = 64f,
            isLeftTurnSignalOn = true,
            isRightTurnSignalOn = false,
            gear = GearPosition.D
        )
        assertEquals(64f, telemetry.speedKmh, 0.01f)
        assertTrue(telemetry.isLeftTurnSignalOn)
        assertFalse(telemetry.isRightTurnSignalOn)
        assertEquals(GearPosition.D, telemetry.gear)

        val config = CarPlayConfig(showHudOverlay = true, hudOverlayCompact = false)
        assertTrue(config.showHudOverlay)
        assertFalse(config.hudOverlayCompact)
    }
}
