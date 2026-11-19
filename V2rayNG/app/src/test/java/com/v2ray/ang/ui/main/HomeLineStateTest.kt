package com.v2ray.ang.ui.main

import com.v2ray.ang.AppConfig
import com.v2ray.ang.dto.OutboundTrafficStat
import com.v2ray.ang.util.proxyTransferBytes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HomeLineStateTest {

    @Test
    fun flagUsesATwoLetterCountryCode() {
        assertEquals("🇮🇷", countryFlagEmoji("ir"))
        assertEquals("🇩🇪", countryFlagEmoji("DE"))
        assertEquals("", countryFlagEmoji("Iran"))
        assertEquals("", countryFlagEmoji(null))
    }

    @Test
    fun oneSecondOfBytesBecomesASpeedAndAGraphPoint() {
        val state = HomeLineState().withTraffic(
            uploadBytes = 2_000,
            downloadBytes = 8_000,
            elapsedMillis = 1_000,
            sessionUploadBytes = 2_000,
            sessionDownloadBytes = 8_000,
        )

        assertEquals(2_000, state.uploadBytesPerSecond)
        assertEquals(8_000, state.downloadBytesPerSecond)
        assertEquals(listOf(8_000f), state.downloadHistory)
        assertEquals(listOf(2_000f), state.uploadHistory)
    }

    @Test
    fun aLongGapUpdatesTheTotalWithoutStretchingTheGraph() {
        val seeded = HomeLineState(downloadHistory = listOf(10f))
        val state = seeded.withTraffic(
            uploadBytes = 60_000,
            downloadBytes = 120_000,
            elapsedMillis = 60_000,
            sessionUploadBytes = 60_000,
            sessionDownloadBytes = 120_000,
        )

        assertEquals(2_000, state.downloadBytesPerSecond)
        assertEquals(listOf(10f), state.downloadHistory)
        assertEquals(120_000, state.sessionDownloadBytes)
    }

    @Test
    fun historyKeepsTheLatestSamplesOnly() {
        var history = emptyList<Float>()
        repeat(HOME_SPEED_HISTORY_SIZE + 5) { index ->
            history = appendSpeedSample(history, index.toLong(), 1_000)
        }
        assertEquals(HOME_SPEED_HISTORY_SIZE, history.size)
        assertEquals(5f, history.first())
        assertEquals((HOME_SPEED_HISTORY_SIZE + 4).toFloat(), history.last())
    }

    @Test
    fun disconnectClearsTrafficAndKeepsTheAddress() {
        val state = HomeLineState(
            ipAddress = "1.2.3.4",
            downloadBytesPerSecond = 10,
            downloadHistory = listOf(10f),
        ).clearedTraffic()

        assertEquals("1.2.3.4", state.ipAddress)
        assertEquals(0, state.downloadBytesPerSecond)
        assertTrue(state.downloadHistory.isEmpty())
    }

    @Test
    fun proxyBytesIgnoreDirectAndBlockedTraffic() {
        val (upload, download) = proxyTransferBytes(
            listOf(
                OutboundTrafficStat(AppConfig.TAG_PROXY, AppConfig.UPLINK, 10),
                OutboundTrafficStat(AppConfig.TAG_PROXY, AppConfig.DOWNLINK, 20),
                OutboundTrafficStat("custom-out", AppConfig.DOWNLINK, 5),
                OutboundTrafficStat(AppConfig.TAG_DIRECT, AppConfig.DOWNLINK, 100),
                OutboundTrafficStat(AppConfig.TAG_BLOCKED, AppConfig.UPLINK, 7),
            )
        )

        assertEquals(10, upload)
        assertEquals(25, download)
    }
}
