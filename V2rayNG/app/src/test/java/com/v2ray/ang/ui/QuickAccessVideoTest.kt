package com.v2ray.ang.ui

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class QuickAccessVideoTest {

    @Test
    fun movAndQuickTimeNeedExternalPlayer() {
        assertTrue(QuickAccessVideo.shouldOpenExternally("https://mbarani.ir/uploads/stories/s_53.mov"))
        assertTrue(QuickAccessVideo.shouldOpenExternally("https://mbarani.ir/a.qt?x=1"))
    }

    @Test
    fun mp4AndWebmStayInWebView() {
        assertFalse(QuickAccessVideo.shouldOpenExternally("https://mbarani.ir/uploads/stories/s.mp4"))
        assertFalse(QuickAccessVideo.shouldOpenExternally("https://mbarani.ir/clip.webm"))
        assertFalse(QuickAccessVideo.shouldOpenExternally(""))
    }
}
