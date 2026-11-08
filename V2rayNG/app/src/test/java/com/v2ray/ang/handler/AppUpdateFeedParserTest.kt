package com.v2ray.ang.handler

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppUpdateFeedParserTest {
    @Test
    fun parsesJsonFeed() {
        val feed = AppUpdateFeedParser.parse(
            """{"version":"2.3.35","apk":"https://example.com/a.apk","notes":"hi"}"""
        )
        assertEquals("2.3.35", feed?.version)
        assertEquals("https://example.com/a.apk", feed?.apk)
        assertEquals("hi", feed?.notes)
    }

    @Test
    fun parsesPlainTextFeed() {
        val feed = AppUpdateFeedParser.parse(
            """
            2.3.35
            https://example.com/a.apk
            line one
            line two
            """.trimIndent()
        )
        assertEquals("2.3.35", feed?.version)
        assertEquals("https://example.com/a.apk", feed?.apk)
        assertEquals("line one\nline two", feed?.notes)
    }

    @Test
    fun rejectsHtmlAsFeed() {
        assertTrue(AppUpdateFeedParser.looksLikeHtml("<!DOCTYPE html><html>"))
        assertNull(AppUpdateFeedParser.parse("<!DOCTYPE html><html><body>nope</body></html>"))
    }

    @Test
    fun versionCompareDetectsNewerRelease() {
        val current = UpdateCheckerManager.versionParts("2.3.31")!!
        val latest = UpdateCheckerManager.versionParts("2.3.35")!!
        assertTrue(UpdateCheckerManager.compareVersions(latest, current) > 0)
        assertFalse(UpdateCheckerManager.compareVersions(current, latest) > 0)
    }
}
