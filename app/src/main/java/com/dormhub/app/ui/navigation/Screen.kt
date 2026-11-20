/*
 * Screen.kt — กำหนด route ของทุกหน้าในแอป
 * ============================================================
 * sealed class ป้องกันการพิมพ์ route ผิด (compile-time safety)
 * ใช้ route string ในการนำทาง เช่น navController.navigate(Screen.Dashboard.route)
 */
package com.dormhub.app.ui.navigation

sealed class Screen(val route: String) {
    // หน้าหลัก
    object Onboarding    : Screen("onboarding")
    object Dashboard     : Screen("dashboard")
    object Rooms         : Screen("rooms")
    object Settings      : Screen("settings")

    // ห้องพัก
    object RoomDetail    : Screen("room/{roomId}") {
        fun withId(id: String) = "room/$id"
    }

    // ผู้เช่า
    object AddTenant     : Screen("room/{roomId}/add-tenant") {
        fun withRoomId(id: String) = "room/$id/add-tenant"
    }
    object EditTenant    : Screen("room/{roomId}/tenant/{tenantId}") {
        fun withIds(roomId: String, tenantId: String) = "room/$roomId/tenant/$tenantId"
    }

    // Settings sub-screens
    object RateSetup     : Screen("settings/rates")
    object FeeSetup      : Screen("settings/fees")
    object BuildingSetup : Screen("settings/buildings")
    object PropertyInfo  : Screen("settings/property")

    // Phase 2 — มิเตอร์และบิล
    object MeterEntry : Screen("meter/{yearMonth}") {
        fun withYearMonth(ym: Int) = "meter/$ym"
    }
    object BillList : Screen("bills/{yearMonth}") {
        fun withYearMonth(ym: Int) = "bills/$ym"
    }
    object BillDetail : Screen("bills/{yearMonth}/{billId}") {
        fun withIds(ym: Int, billId: String) = "bills/$ym/$billId"
    }
    object Payment : Screen("bills/{yearMonth}/{billId}/payment") {
        fun withIds(ym: Int, billId: String) = "bills/$ym/$billId/payment"
    }

    // Phase 3 — Report
    object Report : Screen("report")
}
