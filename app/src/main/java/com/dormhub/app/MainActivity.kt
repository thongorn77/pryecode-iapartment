/*
 * MainActivity.kt — Activity เดียวของแอป (Single-Activity Architecture)
 * ============================================================
 * Jetpack Compose + Navigation ทำให้ใช้ Activity เดียวก็พอ
 * @AndroidEntryPoint บอก Hilt ว่า Activity นี้ต้องการ DI
 * setContent{} คือจุดเริ่มต้นของ Compose UI ทั้งหมด
 */
package com.dormhub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.dormhub.app.ui.navigation.AppNavGraph
import com.dormhub.app.ui.theme.DormHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DormHubTheme {
                AppNavGraph()
            }
        }
    }
}
