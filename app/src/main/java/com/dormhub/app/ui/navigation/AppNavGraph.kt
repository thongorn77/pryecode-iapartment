/*
 * AppNavGraph.kt — กราฟการนำทางทั้งหมดของแอป
 * ============================================================
 * NavHost = container ของหน้าต่างๆ ทั้งหมด
 * startDestination = กำหนดจาก AppViewModel ว่า onboarding เสร็จหรือยัง
 * Bottom Navigation ปรากฏเฉพาะหน้าหลัก 3 หน้า (Dashboard, Rooms, Settings)
 */
package com.dormhub.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.dormhub.app.R
import com.dormhub.app.ui.AppViewModel
import com.dormhub.app.ui.bills.BillDetailScreen
import com.dormhub.app.ui.bills.BillListScreen
import com.dormhub.app.ui.bills.PaymentScreen
import com.dormhub.app.ui.dashboard.DashboardScreen
import com.dormhub.app.ui.meter.MeterEntryScreen
import com.dormhub.app.ui.onboarding.OnboardingScreen
import com.dormhub.app.ui.rooms.RoomDetailScreen
import com.dormhub.app.ui.rooms.RoomsScreen
import com.dormhub.app.ui.settings.SettingsScreen
import com.dormhub.app.ui.tenants.TenantFormScreen

data class BottomNavItem(val screen: Screen, val labelRes: Int, val icon: @Composable () -> Unit)

@Composable
fun AppNavGraph() {
    val appViewModel: AppViewModel = hiltViewModel()
    val onboardingComplete by appViewModel.isOnboardingComplete.collectAsStateWithLifecycle()

    // กำลังโหลด
    if (onboardingComplete == null) {
        CircularProgressLoadingScreen()
        return
    }

    val startDestination = if (onboardingComplete == true) Screen.Dashboard.route
                           else Screen.Onboarding.route

    val navController = rememberNavController()

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard, R.string.nav_dashboard) { Icon(Icons.Default.Dashboard, null) },
        BottomNavItem(Screen.Rooms,     R.string.nav_rooms)     { Icon(Icons.Default.MeetingRoom, null) },
        BottomNavItem(Screen.Settings,  R.string.nav_settings)  { Icon(Icons.Default.Settings, null) },
    )

    val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = currentBackStack?.destination
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = item.icon,
                            label = { Text(stringResource(item.labelRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = startDestination,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(onComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }
            composable(Screen.Rooms.route) {
                RoomsScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
            composable(Screen.RoomDetail.route) { backStack ->
                val roomId = backStack.arguments?.getString("roomId") ?: return@composable
                RoomDetailScreen(roomId = roomId, navController = navController)
            }
            composable(Screen.AddTenant.route) { backStack ->
                val roomId = backStack.arguments?.getString("roomId") ?: return@composable
                TenantFormScreen(roomId = roomId, tenantId = null, navController = navController)
            }
            composable(Screen.EditTenant.route) { backStack ->
                val roomId   = backStack.arguments?.getString("roomId") ?: return@composable
                val tenantId = backStack.arguments?.getString("tenantId") ?: return@composable
                TenantFormScreen(roomId = roomId, tenantId = tenantId, navController = navController)
            }

            // Phase 2 — มิเตอร์และบิล
            composable(Screen.MeterEntry.route) { backStack ->
                val yearMonth = backStack.arguments?.getString("yearMonth")?.toIntOrNull() ?: return@composable
                MeterEntryScreen(yearMonth = yearMonth, navController = navController)
            }
            composable(Screen.BillList.route) { backStack ->
                val yearMonth = backStack.arguments?.getString("yearMonth")?.toIntOrNull() ?: return@composable
                BillListScreen(yearMonth = yearMonth, navController = navController)
            }
            composable(Screen.BillDetail.route) { backStack ->
                val yearMonth = backStack.arguments?.getString("yearMonth")?.toIntOrNull() ?: return@composable
                val billId    = backStack.arguments?.getString("billId") ?: return@composable
                BillDetailScreen(yearMonth = yearMonth, billId = billId, navController = navController)
            }
            composable(Screen.Payment.route) { backStack ->
                val yearMonth = backStack.arguments?.getString("yearMonth")?.toIntOrNull() ?: return@composable
                val billId    = backStack.arguments?.getString("billId") ?: return@composable
                PaymentScreen(yearMonth = yearMonth, billId = billId, navController = navController)
            }
        }
    }
}

@Composable
private fun CircularProgressLoadingScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
