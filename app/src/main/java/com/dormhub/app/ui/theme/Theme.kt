/*
 * Theme.kt — Theme หลักของแอป (Material 3)
 * ============================================================
 * DormHubTheme ครอบ UI ทั้งหมดใน MainActivity
 * dynamicColor = true (Android 12+) ใช้สีตาม wallpaper ของผู้ใช้
 * darkTheme รองรับ Dark Mode อัตโนมัติตามการตั้งค่าระบบ
 */
package com.dormhub.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = DormHubBlue,
    onPrimary        = androidx.compose.ui.graphics.Color.White,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFD6E4FF),
    secondary        = DormHubTeal,
    tertiary         = DormHubAmber,
)

private val DarkColorScheme = darkColorScheme(
    primary          = androidx.compose.ui.graphics.Color(0xFF90CAF9),
    onPrimary        = androidx.compose.ui.graphics.Color(0xFF0D47A1),
    primaryContainer = DormHubBlueDark,
    secondary        = androidx.compose.ui.graphics.Color(0xFF80CBC4),
)

@Composable
fun DormHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
