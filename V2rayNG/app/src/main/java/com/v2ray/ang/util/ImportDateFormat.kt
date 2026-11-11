package com.v2ray.ang.util

import java.time.Instant
import java.time.ZoneId

/**
 * Import timestamp shown beside a profile protocol: Jalali date, then Gregorian,
 * for example `1405/02/01  2026/04/21`. A missing timestamp has no label.
 */
internal fun formatImportDates(
    addedTimeMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    if (addedTimeMillis <= 0L) return ""
    val date = Instant.ofEpochMilli(addedTimeMillis).atZone(zone).toLocalDate()
    val jalali = gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)
    return "%04d/%02d/%02d  %04d/%02d/%02d".format(
        jalali.year,
        jalali.month,
        jalali.day,
        date.year,
        date.monthValue,
        date.dayOfMonth,
    )
}

internal data class JalaliDate(val year: Int, val month: Int, val day: Int)

/**
 * Civil Jalali conversion used by the Iranian calendar (the jalaali breaks table).
 * Positive Gregorian years only; profile timestamps are always after the epoch.
 */
internal fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate = julianDayToJalali(gregorianToJulianDay(gy, gm, gd))

private fun gregorianToJulianDay(gy: Int, gm: Int, gd: Int): Int {
    var day = div((gy + div(gm - 8, 6) + 100100) * 1461, 4) +
        div(153 * mod(gm + 9, 12) + 2, 5) +
        gd - 34840408
    day -= div(div(gy + 100100 + div(gm - 8, 6), 100) * 3, 4) - 752
    return day
}

private fun julianDayToJalali(jdn: Int): JalaliDate {
    val gy = julianDayToGregorian(jdn).year
    var jy = gy - 621
    val calendar = jalaliCalendar(jy)
    val firstFarvardin = gregorianToJulianDay(gy, 3, calendar.march)
    var offset = jdn - firstFarvardin
    if (offset >= 0) {
        if (offset <= 185) {
            return JalaliDate(jy, 1 + div(offset, 31), mod(offset, 31) + 1)
        }
        offset -= 186
    } else {
        jy -= 1
        offset += 179
        if (calendar.leap == 1) offset += 1
    }
    return JalaliDate(jy, 7 + div(offset, 30), mod(offset, 30) + 1)
}

private data class GregorianDate(val year: Int, val month: Int, val day: Int)

private fun julianDayToGregorian(jdn: Int): GregorianDate {
    var shifted = 4 * jdn + 139361631
    shifted += div(div(4 * jdn + 183187720, 146097) * 3, 4) * 4 - 3908
    val packed = div(mod(shifted, 1461), 4) * 5 + 308
    val day = div(mod(packed, 153), 5) + 1
    val month = mod(div(packed, 153), 12) + 1
    val year = div(shifted, 1461) - 100100 + div(8 - month, 6)
    return GregorianDate(year, month, day)
}

private data class JalaliCalendar(val leap: Int, val march: Int)

private fun jalaliCalendar(jy: Int): JalaliCalendar {
    val breaks = intArrayOf(
        -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210,
        1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
    )
    val gy = jy + 621
    var leapJ = -14
    var previous = breaks[0]
    var jump = 0
    for (index in 1 until breaks.size) {
        val current = breaks[index]
        jump = current - previous
        if (jy < current) break
        leapJ += div(jump, 33) * 8 + div(mod(jump, 33), 4)
        previous = current
    }
    var yearInBreak = jy - previous
    leapJ += div(yearInBreak, 33) * 8 + div(mod(yearInBreak, 33) + 3, 4)
    if (mod(jump, 33) == 4 && jump - yearInBreak == 4) leapJ += 1
    val leapG = div(gy, 4) - div((div(gy, 100) + 1) * 3, 4) - 150
    val march = 20 + leapJ - leapG
    if (jump - yearInBreak < 6) yearInBreak = yearInBreak - jump + div(jump + 4, 33) * 33
    var leap = mod(mod(yearInBreak + 1, 33) - 1, 4)
    if (leap == -1) leap = 4
    return JalaliCalendar(leap, march)
}

private fun div(a: Int, b: Int): Int = a / b

private fun mod(a: Int, b: Int): Int = a - div(a, b) * b
