package com.v2ray.ang.handler

import com.v2ray.ang.dto.AppUpdateFeed
import com.v2ray.ang.util.JsonUtil

object AppUpdateFeedParser {
    fun parse(body: String): AppUpdateFeed? {
        val trimmed = body.trim()
        if (trimmed.isEmpty() || looksLikeHtml(trimmed)) return null

        JsonUtil.fromJsonSafe(trimmed, AppUpdateFeed::class.java)
            ?.takeIf { it.version.isNotBlank() && it.apk.isNotBlank() }
            ?.let { return it }

        // Plain-text fallback:
        // version
        // apk-url
        // optional notes...
        val lines = trimmed.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
        if (lines.size < 2) return null
        val version = lines[0].removePrefix("v").trim()
        val apk = lines[1].trim()
        if (!version.firstOrNull()?.isDigit().orFalse()) return null
        if (!apk.startsWith("http://") && !apk.startsWith("https://")) return null
        val notes = lines.drop(2).joinToString("\n").ifBlank { null }
        return AppUpdateFeed(version = version, apk = apk, notes = notes)
    }

    fun looksLikeHtml(body: String): Boolean {
        val head = body.trimStart().take(64).lowercase()
        return head.startsWith("<!doctype") ||
            head.startsWith("<html") ||
            head.startsWith("<head") ||
            head.contains("<html")
    }

    private fun Boolean?.orFalse(): Boolean = this == true
}
