package com.v2ray.ang.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.rive.Result as RiveLoadResult
import app.rive.Rive
import app.rive.RiveFileSource
import app.rive.RiveSemanticsMode
import app.rive.rememberRiveFile
import app.rive.rememberRiveWorkerOrNull
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.v2ray.ang.R
import com.v2ray.ang.util.LogUtil

/**
 * Home identity: a Lottie arc, a Rive orbit, and the connection status.
 * The Rive Compose loader is the current rive-android 11.16 entry point.
 * Reevaluate it when that line publishes a replacement for [rememberRiveFile].
 * The orbit file is the MIT sample circle_move.riv from rive-app/rive-android.
 */
@Composable
internal fun BaranHomeStage(
    statusText: String,
    running: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 18.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
            BaranPulse(Modifier.fillMaxSize())
            BaranOrbitMark(Modifier.size(78.dp))
        }
        Column(Modifier.padding(start = 8.dp)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (running) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun BaranPulse(modifier: Modifier = Modifier) {
    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.baran_pulse)).value
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier,
    )
}

@Composable
internal fun BaranOrbitMark(modifier: Modifier = Modifier) {
    val failure = remember { mutableStateOf<Throwable?>(null) }
    val worker = rememberRiveWorkerOrNull(failure)
    LaunchedEffect(failure.value) {
        val error = failure.value ?: return@LaunchedEffect
        LogUtil.w(message = "Rive orbit, home stage, worker failed", throwable = error)
    }
    if (worker == null) return
    val loaded = rememberRiveFile(RiveFileSource.RawRes.from(R.raw.baran_orbit), worker)
    when (loaded) {
        is RiveLoadResult.Success -> Rive(
            file = loaded.value,
            modifier = modifier,
            semantics = RiveSemanticsMode.Off,
        )
        is RiveLoadResult.Error -> LaunchedEffect(loaded.throwable) {
            LogUtil.w(message = "Rive orbit, home stage, file failed", throwable = loaded.throwable)
        }
        is RiveLoadResult.Loading -> Unit
    }
}
