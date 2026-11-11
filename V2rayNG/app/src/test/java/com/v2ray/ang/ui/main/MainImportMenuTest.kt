package com.v2ray.ang.ui.main

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MainImportMenuTest {

    @Test
    fun collapsedImportMenuShowsOnlyTheFirstFiveActions() {
        assertEquals(
            ImportMenuAction.entries.take(IMPORT_MENU_COLLAPSED_COUNT),
            importMenuVisibleActions(showAllItems = false),
        )
    }

    @Test
    fun expandedImportMenuShowsEveryAction() {
        assertEquals(ImportMenuAction.entries, importMenuVisibleActions(showAllItems = true))
    }

    @Test
    fun regularShareMenuContainsOnlyShareActions() {
        val expected = listOf(
            ServerMenuAction.ShareQRCode,
            ServerMenuAction.ShareClipboard,
            ServerMenuAction.ShareFullContent,
        )
        assertEquals(expected, serverMenuActions(isComplexProfile = false, includeManagementActions = false))
    }

    @Test
    fun regularMoreMenuContainsEveryActionInDisplayOrder() {
        assertEquals(
            ServerMenuAction.entries,
            serverMenuActions(isComplexProfile = false, includeManagementActions = true),
        )
    }

    @Test
    fun complexShareMenuContainsOnlyFullContent() {
        assertEquals(
            listOf(ServerMenuAction.ShareFullContent),
            serverMenuActions(isComplexProfile = true, includeManagementActions = false),
        )
    }

    @Test
    fun complexMoreMenuRetainsManagementActions() {
        val expected = listOf(
            ServerMenuAction.ShareFullContent,
            ServerMenuAction.RealPing,
            ServerMenuAction.Edit,
            ServerMenuAction.Rename,
            ServerMenuAction.Delete,
        )
        assertEquals(expected, serverMenuActions(isComplexProfile = true, includeManagementActions = true))
    }
}
