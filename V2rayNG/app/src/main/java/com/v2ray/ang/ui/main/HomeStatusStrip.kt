package com.v2ray.ang.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R
import com.v2ray.ang.extension.toSpeedString
import com.v2ray.ang.extension.toTrafficString

private val DownloadColor = Color(0xFF1B8A4A)
private val UploadColor = Color(0xFF7C4DFF)

@Composable
fun HomeStatusStrip(
    mainViewModel: MainViewModel,
    onRefresh: () -> Unit,
) {
    val line by mainViewModel.homeLine.collectAsStateWithLifecycle()
    var showFullIp by rememberSaveable(line.ipAddress) { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]
    val countryName = countryDisplayName(line.countryCode, locale)
    val label = if (line.throughVpn && !countryName.isNullOrBlank()) {
        stringResource(R.string.home_line_vpn_country, countryName)
    } else {
        stringResource(if (line.throughVpn) R.string.home_line_vpn else R.string.home_line_direct)
    }
    val canRevealIp = line.throughVpn && !line.ipAddress.isNullOrBlank()
    val ipText = when {
        line.ipAddress.isNullOrBlank() -> stringResource(
            if (line.checking) R.string.home_line_unknown else R.string.home_line_unavailable
        )
        canRevealIp && !showFullIp -> maskIpAddress(line.ipAddress)
        else -> line.ipAddress.orEmpty()
    }
    val ipAction = stringResource(if (showFullIp) R.string.home_line_ip_hide else R.string.home_line_ip_show)
    val downloadText = stringResource(R.string.home_speed_down, line.downloadBytesPerSecond.toSpeedString())
    val uploadText = stringResource(R.string.home_speed_up, line.uploadBytesPerSecond.toSpeedString())
    val trafficText = stringResource(
        R.string.home_traffic_session,
        (line.sessionUploadBytes + line.sessionDownloadBytes).toTrafficString()
    )
    val summary = stringResource(R.string.home_line_summary, label, ipText, downloadText, uploadText, trafficText)
    val flag = countryFlagEmoji(line.countryCode)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = summary
            }
            .clickable(onClick = onRefresh),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    ) {
        Column(
            modifier = Modifier
                .clearAndSetSemantics {}
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = flag.ifEmpty { "\uD83C\uDF10" }, fontSize = 26.sp)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp)
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ipText,
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier
                            .semantics {
                                role = Role.Button
                                contentDescription = if (canRevealIp) "$ipText, $ipAction" else ipText
                            }
                            .clickable {
                                if (canRevealIp) showFullIp = !showFullIp else onRefresh()
                            }
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "↓ ${line.downloadBytesPerSecond.toSpeedString()}", color = DownloadColor, fontWeight = FontWeight.SemiBold)
                    Text(text = "↑ ${line.uploadBytesPerSecond.toSpeedString()}", color = UploadColor, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(
                text = trafficText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SpeedSparkline(
                download = line.downloadHistory,
                upload = line.uploadHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            )
        }
    }
}

@Composable
private fun SpeedSparkline(
    download: List<Float>,
    upload: List<Float>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawLine(
            color = Color.Gray.copy(alpha = 0.28f),
            start = Offset(0f, size.height - 1.dp.toPx()),
            end = Offset(size.width, size.height - 1.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
        drawSpeedPath(download, DownloadColor)
        drawSpeedPath(upload, UploadColor)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSpeedPath(values: List<Float>, color: Color) {
    if (values.isEmpty()) return
    val maxValue = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
    if (values.size == 1) {
        val y = size.height - (values[0] / maxValue) * size.height
        drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(size.width, y))
        return
    }
    val points = values.mapIndexed { index, value ->
        Offset(
            x = index.toFloat() / values.lastIndex.toFloat() * size.width,
            y = size.height - (value / maxValue) * (size.height - 2.dp.toPx()),
        )
    }
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        for (index in 0 until points.lastIndex) {
            val start = points[index]
            val end = points[index + 1]
            val midX = (start.x + end.x) / 2f
            cubicTo(midX, start.y, midX, end.y, end.x, end.y)
        }
    }
    val fill = Path().apply {
        addPath(path)
        lineTo(points.last().x, size.height)
        lineTo(points.first().x, size.height)
        close()
    }
    drawPath(path = fill, color = color.copy(alpha = 0.16f))
    drawPath(path = path, color = color, style = stroke)
}
