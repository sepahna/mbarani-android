package com.v2ray.ang.ui.main

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.IOException

class MainRenameTest {
    @Test
    fun initialNameIsKeptUntilADifferentNameIsAccepted() {
        val stored = StoredName("Old")
        val outcome = applyStoredRename(
            requestedName = "  New name  ",
            allowed = true,
            currentName = stored::current,
            save = stored::save,
        )

        assertEquals(RenameOutcome.Updated, outcome)
        assertEquals("New name", stored.current())
        assertEquals(1, stored.saves)
    }

    @Test
    fun emptyAndWhitespaceNamesAreRejected() {
        for (requested in listOf("", "   ", "\n\t")) {
            val stored = StoredName("Old")
            val outcome = applyStoredRename(
                requestedName = requested,
                allowed = true,
                currentName = stored::current,
                save = stored::save,
            )

            assertEquals(RenameOutcome.Empty, outcome)
            assertEquals("Old", stored.current())
            assertEquals(0, stored.saves)
        }
    }

    @Test
    fun disallowedTargetIsRejectedBeforeReadingStorage() {
        var reads = 0
        val outcome = applyStoredRename(
            requestedName = "Name",
            allowed = false,
            currentName = {
                reads += 1
                "Old"
            },
            save = { error("save") },
        )

        assertEquals(RenameOutcome.NotAllowed, outcome)
        assertEquals(0, reads)
    }

    @Test
    fun missingRecordIsRejected() {
        var saved = false
        val outcome = applyStoredRename(
            requestedName = "Name",
            allowed = true,
            currentName = { null },
            save = { saved = true },
        )

        assertEquals(RenameOutcome.Missing, outcome)
        assertEquals(false, saved)
    }

    @Test
    fun unchangedNameDoesNotWrite() {
        val stored = StoredName("Same")
        val outcome = applyStoredRename(
            requestedName = "  Same  ",
            allowed = true,
            currentName = stored::current,
            save = stored::save,
        )

        assertEquals(RenameOutcome.Unchanged, outcome)
        assertEquals(0, stored.saves)
        assertEquals("Same", stored.current())
    }

    @Test
    fun storageFailureIsReturnedWithTheException() {
        val failure = IOException("disk")
        val outcome = applyStoredRename(
            requestedName = "Name",
            allowed = true,
            currentName = { "Old" },
            save = { throw failure },
        )

        assertTrue(outcome is RenameOutcome.Failed)
        assertEquals(failure, (outcome as RenameOutcome.Failed).error)
    }

    @Test
    fun readFailureIsReturnedWithTheException() {
        val failure = IllegalStateException("closed")
        val outcome = applyStoredRename(
            requestedName = "Name",
            allowed = true,
            currentName = { throw failure },
            save = { error("save") },
        )

        assertTrue(outcome is RenameOutcome.Failed)
        assertEquals(failure, (outcome as RenameOutcome.Failed).error)
    }

    private class StoredName(private var name: String) {
        var saves: Int = 0
            private set

        fun current(): String = name

        fun save(value: String) {
            saves += 1
            name = value
        }
    }
}
