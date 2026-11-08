package com.v2ray.ang.ui.main

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AutoPingDelayClearTest {
    @Test
    fun autoTcpPathPreservesExistingDelayLabels() {
        assertFalse(shouldClearDelaysBeforeBulkPing(preserveExistingDelays = true))
    }

    @Test
    fun manualBulkPathClearsExistingDelayLabels() {
        assertTrue(shouldClearDelaysBeforeBulkPing(preserveExistingDelays = false))
    }
}
