package com.v2ray.ang.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.v2ray.ang.R
import com.v2ray.ang.dto.entities.ServersCache

@Composable
internal fun NamePromptDialog(
    title: String,
    initialName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable(initialName) { mutableStateOf(initialName) }
    val trimmed = name.trim()
    val submit = {
        if (trimmed.isNotEmpty()) onConfirm(trimmed)
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .consumeWindowInsets(WindowInsets.navigationBars)
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.group_tab_rename_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        selectionColors = TextSelectionColors(
                            handleColor = MaterialTheme.colorScheme.secondary,
                            backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    TextButton(onClick = submit, enabled = trimmed.isNotEmpty()) {
                        Text(stringResource(R.string.action_ok))
                    }
                }
            }
        }
    }
}

@Composable
internal fun ServerMoveDialog(
    servers: List<ServersCache>,
    guid: String,
    onMoveBefore: (String) -> Unit,
    onMoveLast: () -> Unit,
    onDismiss: () -> Unit,
) {
    val index = servers.indexOfFirst { it.guid == guid }
    val beforeTargets = servers.filter { other ->
        val otherIndex = servers.indexOfFirst { it.guid == other.guid }
        other.guid != guid &&
            otherIndex != index + 1 &&
            !(index > 0 && other.guid == servers.first().guid)
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.padding(top = 20.dp, bottom = 8.dp)) {
                Text(
                    stringResource(R.string.server_move_title),
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                Column(
                    Modifier
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (servers.size < 2 || index < 0) {
                        Text(
                            stringResource(R.string.server_move_only_one),
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    } else {
                        if (index > 0) {
                            TextButton(
                                onClick = { onMoveBefore(servers.first().guid) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.group_tab_move_first),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        beforeTargets.forEach { other ->
                            TextButton(
                                onClick = { onMoveBefore(other.guid) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    stringResource(R.string.group_tab_move_before, other.profile.remarks),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        if (index < servers.lastIndex) {
                            TextButton(onClick = onMoveLast, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    stringResource(R.string.group_tab_move_last),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_cancel), modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
