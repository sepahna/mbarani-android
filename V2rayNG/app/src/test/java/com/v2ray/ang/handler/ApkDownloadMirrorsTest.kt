package com.v2ray.ang.handler

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApkDownloadMirrorsTest {
    @Test
    fun githubDownloadsGetMirrorFallbacks() {
        val url = "https://github.com/sepahna/mbarani-android/releases/download/v2.3.35/a.apk"
        val candidates = ApkDownloadMirrors.candidates(url)
        assertEquals(url, candidates.first())
        assertTrue(candidates.any { it.startsWith("https://ghfast.top/") })
        assertTrue(candidates.any { it.startsWith("https://ghproxy.net/") })
        assertTrue(candidates.any { it.startsWith("https://gh-proxy.com/") })
    }

    @Test
    fun nonGithubUrlStaysSingleCandidate() {
        val url = "https://mbarani.ir/downloads/a.apk"
        assertEquals(listOf(url), ApkDownloadMirrors.candidates(url))
    }
}
