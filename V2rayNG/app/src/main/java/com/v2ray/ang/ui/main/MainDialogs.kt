package com.v2ray.ang.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.res.stringResource
import com.v2ray.ang.R
import com.v2ray.ang.ui.compose.DeleteConfirmDialog

data class ServerDeleteTarget(
    val guid: String,
    val profileName: String,
) {
    companion object {
        val Saver = listSaver<ServerDeleteTarget?, String>(
            save = { target -> target?.let { listOf(it.guid, it.profileName) }.orEmpty() },
            restore = { ServerDeleteTarget(it[0], it[1]) },
        )
    }
}

data class GroupRenameTarget(
    val groupId: String,
    val name: String,
) {
    companion object {
        val Saver = listSaver<GroupRenameTarget?, String>(
            save = { target -> target?.let { listOf(it.groupId, it.name) }.orEmpty() },
            restore = { GroupRenameTarget(it[0], it[1]) },
        )
    }
}

data class ServerRenameTarget(
    val guid: String,
    val name: String,
) {
    companion object {
        val Saver = listSaver<ServerRenameTarget?, String>(
            save = { target -> target?.let { listOf(it.guid, it.name) }.orEmpty() },
            restore = { ServerRenameTarget(it[0], it[1]) },
        )
    }
}

data class ServerMoveTarget(
    val groupId: String,
    val guid: String,
) {
    companion object {
        val Saver = listSaver<ServerMoveTarget?, String>(
            save = { target -> target?.let { listOf(it.groupId, it.guid) }.orEmpty() },
            restore = { ServerMoveTarget(it[0], it[1]) },
        )
    }
}

@Composable
fun MainDialogs(
    showDelAllConfirm: Boolean,
    onDismissDelAll: () -> Unit,
    onConfirmDelAll: () -> Unit,
    showDelDuplicateConfirm: Boolean,
    onDismissDelDuplicate: () -> Unit,
    onConfirmDelDuplicate: () -> Unit,
    showDelInvalidConfirm: Boolean,
    onDismissDelInvalid: () -> Unit,
    onConfirmDelInvalid: () -> Unit,
    showRemoveConfirm: ServerDeleteTarget?,
    onDismissRemove: () -> Unit,
    onConfirmRemove: (String) -> Unit,
) {
    if (showDelAllConfirm) {
        DeleteConfirmDialog(
            message = stringResource(R.string.confirm_delete_visible_profiles),
            onConfirm = onConfirmDelAll,
            onDismiss = onDismissDelAll
        )
    }
    if (showDelDuplicateConfirm) {
        DeleteConfirmDialog(
            message = stringResource(R.string.confirm_delete_duplicate_profiles),
            onConfirm = onConfirmDelDuplicate,
            onDismiss = onDismissDelDuplicate
        )
    }
    if (showDelInvalidConfirm) {
        DeleteConfirmDialog(
            message = stringResource(R.string.confirm_delete_invalid_profiles),
            onConfirm = onConfirmDelInvalid,
            onDismiss = onDismissDelInvalid
        )
    }
    showRemoveConfirm?.let { target ->
        DeleteConfirmDialog(
            message = stringResource(R.string.confirm_delete_profile),
            itemName = target.profileName,
            onConfirm = { onConfirmRemove(target.guid) },
            onDismiss = onDismissRemove
        )
    }
}
