package com.v2ray.ang.ui.checkupdate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.v2ray.ang.R
import com.v2ray.ang.handler.AppUpdateUi

@Composable
fun UpdateProgressDialog(
    state: AppUpdateUi,
    onDismiss: () -> Unit
) {
    val percent = if (state.total > 0) {
        ((state.received * 100) / state.total).toInt().coerceIn(0, 100)
    } else {
        null
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.update_new_version_found, state.version))
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = when {
                        state.error != null -> stringResource(R.string.update_download_failed)
                        state.finished -> stringResource(R.string.update_install_ready)
                        else -> stringResource(R.string.update_downloading)
                    }
                )
                if (state.error == null && !state.finished) {
                    if (percent == null) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                    } else {
                        LinearProgressIndicator(
                            progress = { percent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        )
                        Text(
                            text = stringResource(R.string.update_progress_percent, percent),
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                state.error?.let {
                    Text(
                        text = it,
                        modifier = Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(if (state.finished || state.error != null) R.string.action_ok else R.string.action_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
