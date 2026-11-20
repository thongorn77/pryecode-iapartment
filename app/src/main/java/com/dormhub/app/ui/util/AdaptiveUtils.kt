package com.dormhub.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/** true เมื่อหน้าจอกว้าง ≥ 600dp (tablet หรือ phone landscape) */
@Composable
fun isWideScreen(): Boolean = LocalConfiguration.current.screenWidthDp >= 600
