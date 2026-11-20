/*
 * Color.kt — สีทั้งหมดของแอป
 * ============================================================
 * ใช้ Material 3 Dynamic Color พร้อม seed color สีน้ำเงิน-เขียว
 * เจ้าของหอพักสามารถเปลี่ยน theme ได้ใน Settings (Phase 6)
 * DormHubBlue = สีหลักของแอป
 */
package com.dormhub.app.ui.theme

import androidx.compose.ui.graphics.Color

val DormHubBlue       = Color(0xFF1565C0)
val DormHubBlueDark   = Color(0xFF0D47A1)
val DormHubTeal       = Color(0xFF00695C)
val DormHubAmber      = Color(0xFFFF8F00)

// สถานะห้อง
val ColorOccupied     = Color(0xFF388E3C)   // เขียว = มีผู้เช่า
val ColorVacant       = Color(0xFF9E9E9E)   // เทา = ว่าง
val ColorMaintenance  = Color(0xFFF57C00)   // ส้ม = ซ่อมบำรุง

// สถานะบิล
val ColorDraft        = Color(0xFF9E9E9E)
val ColorIssued       = Color(0xFF1976D2)
val ColorPartial      = Color(0xFFE65100)
val ColorPaid         = Color(0xFF388E3C)
val ColorVoid         = Color(0xFFB71C1C)
