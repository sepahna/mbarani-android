package com.v2ray.ang.ui.main

const val HOME_SPEED_HISTORY_SIZE = 40
private const val SAMPLE_MIN_MILLIS = 500L
private const val SAMPLE_MAX_MILLIS = 2_500L

data class HomeLineState(
    val throughVpn: Boolean = false,
    val checking: Boolean = false,
    val ipAddress: String? = null,
    val countryCode: String? = null,
    val uploadBytesPerSecond: Long = 0,
    val downloadBytesPerSecond: Long = 0,
    val sessionUploadBytes: Long = 0,
    val sessionDownloadBytes: Long = 0,
    val uploadHistory: List<Float> = emptyList(),
    val downloadHistory: List<Float> = emptyList(),
)

fun HomeLineState.withTraffic(
    uploadBytes: Long,
    downloadBytes: Long,
    elapsedMillis: Long,
    sessionUploadBytes: Long,
    sessionDownloadBytes: Long,
): HomeLineState {
    val uploadBps = bytesPerSecond(uploadBytes, elapsedMillis)
    val downloadBps = bytesPerSecond(downloadBytes, elapsedMillis)
    return copy(
        uploadBytesPerSecond = uploadBps,
        downloadBytesPerSecond = downloadBps,
        sessionUploadBytes = sessionUploadBytes,
        sessionDownloadBytes = sessionDownloadBytes,
        uploadHistory = appendSpeedSample(uploadHistory, uploadBps, elapsedMillis),
        downloadHistory = appendSpeedSample(downloadHistory, downloadBps, elapsedMillis),
    )
}

fun HomeLineState.clearedTraffic(): HomeLineState = copy(
    uploadBytesPerSecond = 0,
    downloadBytesPerSecond = 0,
    sessionUploadBytes = 0,
    sessionDownloadBytes = 0,
    uploadHistory = emptyList(),
    downloadHistory = emptyList(),
)

internal fun bytesPerSecond(bytes: Long, elapsedMillis: Long): Long {
    if (elapsedMillis <= 0L) return 0L
    return bytes.coerceAtLeast(0L) * 1000L / elapsedMillis
}

internal fun appendSpeedSample(history: List<Float>, bytesPerSecond: Long, elapsedMillis: Long): List<Float> {
    if (elapsedMillis !in SAMPLE_MIN_MILLIS..SAMPLE_MAX_MILLIS) return history
    val next = ArrayList<Float>(minOf(history.size + 1, HOME_SPEED_HISTORY_SIZE))
    val start = (history.size - HOME_SPEED_HISTORY_SIZE + 1).coerceAtLeast(0)
    for (index in start until history.size) {
        next.add(history[index])
    }
    next.add(bytesPerSecond.coerceAtLeast(0L).toFloat())
    return next
}

/** First IPv4 group stays visible. The rest of a VPN address stays hidden until the user asks. */
fun maskIpAddress(ipAddress: String?): String {
    val ip = ipAddress?.trim().orEmpty()
    if (ip.isEmpty()) return ""
    val groups = ip.split('.')
    if (groups.size == 4 && groups.all { group -> group.isNotEmpty() && group.all(Char::isDigit) }) {
        return groups[0] + ".***.***.***"
    }
    val v6 = ip.split(':')
    if (v6.size > 1) {
        val head = v6.firstOrNull { it.isNotEmpty() } ?: "*"
        return "$head:****"
    }
    return ip
}

fun countryDisplayName(country: String?, locale: java.util.Locale): String? {
    val value = country?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    if (value.length == 2 && value.all { it.isLetter() }) {
        val name = java.util.Locale("", value.uppercase()).getDisplayCountry(locale)
        if (name.isNotBlank() && !name.equals(value, ignoreCase = true)) return name
    }
    return value
}

fun countryFlagEmoji(countryCode: String?): String {
    val code = countryCode?.trim()?.uppercase() ?: return ""
    if (code.length != 2 || code.any { it !in 'A'..'Z' }) return ""
    val base = 0x1F1E6
    return String(Character.toChars(base + (code[0] - 'A'))) +
        String(Character.toChars(base + (code[1] - 'A')))
}
