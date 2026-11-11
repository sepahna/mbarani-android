package com.v2ray.ang.ui.main

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class MainReorderTest {
    @Test
    fun moveBeforeALaterItemKeepsTheTargetInPlace() {
        assertEquals(1, reorderDestination(size = 4, from = 0, before = 2))
    }

    @Test
    fun moveBeforeAnEarlierItemUsesThatIndex() {
        assertEquals(0, reorderDestination(size = 4, from = 2, before = 0))
    }

    @Test
    fun nullTargetMovesToTheEnd() {
        assertEquals(3, reorderDestination(size = 4, from = 0, before = null))
    }

    @Test
    fun alreadyAtTheDestinationIsUnchanged() {
        assertNull(reorderDestination(size = 4, from = 3, before = null))
        assertNull(reorderDestination(size = 4, from = 1, before = 2))
    }

    @Test
    fun invalidIndexesAndASingleItemAreRejected() {
        assertNull(reorderDestination(size = 4, from = -1, before = 0))
        assertNull(reorderDestination(size = 4, from = 4, before = 0))
        assertNull(reorderDestination(size = 4, from = 1, before = 1))
        assertNull(reorderDestination(size = 4, from = 1, before = 9))
        assertNull(reorderDestination(size = 1, from = 0, before = null))
        assertNull(reorderDestination(size = 0, from = 0, before = null))
    }
}
