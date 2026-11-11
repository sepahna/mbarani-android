package com.v2ray.ang.ui.compose

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.v2ray.ang.AppConfig
import com.v2ray.ang.R
import com.v2ray.ang.handler.MmkvManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val LightColor = lightColorScheme(
    primary = Color(0xFF0C6B58),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFB7F3E4),
    onPrimaryContainer = Color(0xFF002019),
    secondary = Color(0xFF5A3D98),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DDFF),
    onSecondaryContainer = Color(0xFF23005A),
    tertiary = Color(0xFF8A4E12),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC2),
    onTertiaryContainer = Color(0xFF2E1500),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color(0xFFFFFFFF),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xF2F6F1E8),
    onBackground = Color(0xFF1C1915),
    surface = Color(0xF7FFF8F1),
    onSurface = Color(0xFF1C1915),
    surfaceVariant = Color(0xFFE7DDD0),
    onSurfaceVariant = Color(0xFF4E463C),
    outline = Color(0xFF7E756A),
    outlineVariant = Color(0xFFD4C8B8),
    inverseSurface = Color(0xFF312C26),
    inverseOnSurface = Color(0xFFF8F1E7),
    inversePrimary = Color(0xFF7DFFD6),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF0C6B58),
    surfaceContainerLowest = Color(0xFFFFFBF6),
    surfaceContainerLow = Color(0xFFF8F2E8),
    surfaceContainer = Color(0xFFF1E9DC),
    surfaceContainerHigh = Color(0xFFEADFCE),
    surfaceContainerHighest = Color(0xFFE2D4C0),
)

private val DarkColor = darkColorScheme(
    primary = Color(0xFF7DFFD6),
    onPrimary = Color(0xFF00382C),
    primaryContainer = Color(0xFF0E4F42),
    onPrimaryContainer = Color(0xFFB8FFE8),
    secondary = Color(0xFFD4C4FF),
    onSecondary = Color(0xFF3A2172),
    secondaryContainer = Color(0xFF4E3488),
    onSecondaryContainer = Color(0xFFEDE4FF),
    tertiary = Color(0xFFFFC56B),
    onTertiary = Color(0xFF452A00),
    tertiaryContainer = Color(0xFF6A4310),
    onTertiaryContainer = Color(0xFFFFE3B8),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xF2070910),
    onBackground = Color(0xFFE8EEF6),
    surface = Color(0xF2141824),
    onSurface = Color(0xFFE8EEF6),
    surfaceVariant = Color(0xFF2A3142),
    onSurfaceVariant = Color(0xFFC5CEDD),
    outline = Color(0xFF8B97AB),
    outlineVariant = Color(0xFF3A4356),
    inverseSurface = Color(0xFFE8EEF6),
    inverseOnSurface = Color(0xFF12151C),
    inversePrimary = Color(0xFF0C6B58),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFF7DFFD6),
    surfaceContainerLowest = Color(0xFF070910),
    surfaceContainerLow = Color(0xFF121722),
    surfaceContainer = Color(0xFF1A2130),
    surfaceContainerHigh = Color(0xFF232B3C),
    surfaceContainerHighest = Color(0xFF2C364A),
)

private val BaranFont = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.vazirmatn_bold, FontWeight.Bold),
)

private val BaranShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

private fun baranTypography(): Typography {
    val base = Typography()
    fun TextStyle.face() = copy(fontFamily = BaranFont)
    return base.copy(
        displayLarge = base.displayLarge.face(),
        displayMedium = base.displayMedium.face(),
        displaySmall = base.displaySmall.face(),
        headlineLarge = base.headlineLarge.face(),
        headlineMedium = base.headlineMedium.face(),
        headlineSmall = base.headlineSmall.face(),
        titleLarge = base.titleLarge.face(),
        titleMedium = base.titleMedium.face(),
        titleSmall = base.titleSmall.face(),
        bodyLarge = base.bodyLarge.face(),
        bodyMedium = base.bodyMedium.face(),
        bodySmall = base.bodySmall.face(),
        labelLarge = base.labelLarge.face(),
        labelMedium = base.labelMedium.face(),
        labelSmall = base.labelSmall.face(),
    )
}

// Semantic Colors
val colorPing = Color(0xFF12A37A)
val colorPingSlow = Color(0xFFE0A04A)
val colorPingIdle = Color(0xFF8A93A3)
val colorPingRed = Color(0xFFFF5C8A)
val colorConfigType = Color(0xFF0C6B58)
val colorFabActive = Color(0xFF0C6B58)
val colorFabInactiveLight = Color(0xFF8D8376)
val colorFabInactiveDark = Color(0xFF3A4356)
val dividerColorLight = Color(0xFFE0E0E0) // Light Gray
val dividerColorDark = Color(0xFF424242) // Dark Gray

// Toast Colors 85%
val toastNormalBgLight = Color(0xD9353A3E) // Dark Gray
val toastNormalBgDark = Color(0xD94A4F54) // Darker Gray
val toastSuccessBg = Color(0xD9388E3C) // Green
val toastErrorBg = Color(0xD9D50000) // Red
val toastInfoBg = Color(0xD93F51B5) // Indigo Blue
val toastIconCircleBg = Color(0x33FFFFFF) // Semi-transparent White
val toastTextColor = Color.White // White

object ThemeManager {
    private val _themeMode = MutableStateFlow(
        MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
    )
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _dynamicColorEnabled = MutableStateFlow(
        MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, true)
    )
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    fun setThemeMode(mode: String) {
        MmkvManager.encodeSettings(AppConfig.PREF_UI_MODE_NIGHT, mode)
        _themeMode.value = mode
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        MmkvManager.encodeSettings(AppConfig.PREF_DYNAMIC_COLOR, enabled)
        _dynamicColorEnabled.value = enabled
    }

    fun refresh() {
        _themeMode.value =
            MmkvManager.decodeSettingsString(AppConfig.PREF_UI_MODE_NIGHT, "0") ?: "0"
        _dynamicColorEnabled.value =
            MmkvManager.decodeSettingsBool(AppConfig.PREF_DYNAMIC_COLOR, true)
    }
}

@Composable
fun resolveDarkTheme(): Boolean {
    val mode by ThemeManager.themeMode.collectAsState()
    return when (mode) {
        "1" -> false
        "2" -> true
        else -> isSystemInDarkTheme()
    }
}

val LocalDarkTheme = compositionLocalOf { false }

@Composable
fun AppTheme(
    darkTheme: Boolean = resolveDarkTheme(),
    content: @Composable () -> Unit
) {
    val dynamicColor by ThemeManager.dynamicColorEnabled.collectAsState()
    val context = LocalContext.current
    val baran = if (darkTheme) DarkColor else LightColor
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val dynamic = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        baran.copy(
            primary = dynamic.primary,
            onPrimary = dynamic.onPrimary,
            primaryContainer = dynamic.primaryContainer,
            onPrimaryContainer = dynamic.onPrimaryContainer,
            secondary = dynamic.secondary,
            onSecondary = dynamic.onSecondary,
            secondaryContainer = dynamic.secondaryContainer,
            onSecondaryContainer = dynamic.onSecondaryContainer,
        )
    } else {
        baran
    }
    val snackbarController = rememberAppSnackbarController()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalAppSnackbar provides snackbarController
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = BaranShapes,
            typography = baranTypography(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                BaranAtmosphere(darkTheme = darkTheme, modifier = Modifier.fillMaxSize())
                AppSnackbarBridge(controller = snackbarController)
                content()
                AppSnackbarHost(hostState = snackbarController.hostState)
            }
        }
    }
}
