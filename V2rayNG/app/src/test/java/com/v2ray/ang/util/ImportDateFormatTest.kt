package com.v2ray.ang.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ImportDateFormatTest {
    private val tehran = ZoneId.of("Asia/Tehran")

    @Test
    fun missingTimestampHasNoLabel() {
        assertEquals("", formatImportDates(0L, tehran))
        assertEquals("", formatImportDates(-1L, tehran))
    }

    @Test
    fun showsJalaliThenGregorianForNowruz() {
        assertEquals("1403/01/01  2024/03/20", formatImportDates(tehranMillis(2024, 3, 20), tehran))
        assertEquals("1404/01/01  2025/03/21", formatImportDates(tehranMillis(2025, 3, 21), tehran))
        assertEquals("1405/01/01  2026/03/21", formatImportDates(tehranMillis(2026, 3, 21), tehran))
    }

    @Test
    fun padsBothCalendarsAndUsesTheDeviceDate() {
        assertEquals("1405/02/01  2026/04/21", formatImportDates(tehranMillis(2026, 4, 21), tehran))
        val justBeforeTehranMidnight = LocalDateTime.of(2026, 3, 20, 20, 29)
            .atZone(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
        assertEquals("1404/12/29  2026/03/20", formatImportDates(justBeforeTehranMidnight, tehran))
        assertEquals("1405/01/01  2026/03/21", formatImportDates(justBeforeTehranMidnight + 60_000L, tehran))
    }

    private fun tehranMillis(year: Int, month: Int, day: Int): Long =
        LocalDateTime.of(year, month, day, 12, 0)
            .atZone(tehran)
            .toInstant()
            .toEpochMilli()
}
