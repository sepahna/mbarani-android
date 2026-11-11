package com.v2ray.ang.ui.compose

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush

/**
 * Full-window light field. AGSL [RuntimeShader] needs API 33; older devices draw the same
 * palette as a static gradient. Remove the gradient branch when minSdk is 33.
 */
@Composable
internal fun BaranAtmosphere(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val time by rememberInfiniteTransition(label = "baran-field").animateFloat(
        initialValue = 0f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 120_000, easing = LinearEasing)),
        label = "baran-time",
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        BaranShaderField(time = time, darkTheme = darkTheme, modifier = modifier)
    } else {
        val colors = if (darkTheme) {
            listOf(Color(0xFF10182A), Color(0xFF07110F), Color(0xFF1A1030))
        } else {
            listOf(Color(0xFFF7F1E6), Color(0xFFE7F6F1), Color(0xFFF3E8F8))
        }
        Box(modifier.background(Brush.linearGradient(colors)))
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun BaranShaderField(
    time: Float,
    darkTheme: Boolean,
    modifier: Modifier,
) {
    val shader = androidx.compose.runtime.remember { RuntimeShader(BARAN_FIELD_AGSL) }
    val night = if (darkTheme) 1f else 0f
    Box(
        modifier.drawBehind {
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("time", time)
            shader.setFloatUniform("night", night)
            drawRect(ShaderBrush(shader))
        }
    )
}

private const val BARAN_FIELD_AGSL = """
uniform float2 resolution;
uniform float time;
uniform float night;

half4 main(float2 coord) {
    float2 uv = coord / resolution;
    float drift = sin(uv.x * 5.5 + time * 0.65) * 0.055;
    float ribbon = exp(-pow((uv.y - 0.2 - drift) * 3.8, 2.0));
    float2 orbCenter = float2(0.8, 0.14);
    float orb = exp(-dot(uv - orbCenter, uv - orbCenter) * 16.0);
    float lower = exp(-pow((uv.y - 0.92) * 2.4, 2.0));
    half3 ink = half3(0.027, 0.035, 0.063);
    half3 paper = half3(0.965, 0.945, 0.905);
    half3 base = mix(paper, ink, night);
    half3 teal = half3(0.22, 0.86, 0.74);
    half3 violet = half3(0.52, 0.34, 0.95);
    half3 color = base;
    color = mix(color, teal, ribbon * (0.28 + night * 0.42));
    color = mix(color, violet, orb * (0.4 + night * 0.4));
    color = mix(color, teal, lower * night * 0.18);
    return half4(color, 1.0);
}
"""
