/*
 * PropertySettings.kt — ข้อมูลตั้งค่าหอพัก (Singleton Row)
 * ============================================================
 * ตารางนี้มีแค่ 1 แถวเสมอ (id = "default")
 * เก็บทุกอย่างที่เกี่ยวกับเจ้าของหอพักและกฎการออกบิล
 * เงินทุกค่าเก็บเป็น Long หน่วย "สตางค์" (100 สตางค์ = 1 บาท)
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "property_settings")
data class PropertySettings(
    @PrimaryKey val id: String = "default",
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val logoPath: String? = null,
    val promptPayId: String = "",           // หมายเลขโทรศัพท์ / เลขบัตรประชาชน
    val bankName: String = "",
    val bankAccountName: String = "",
    val bankAccountNumber: String = "",
    val billFooterNote: String = "",        // ข้อความท้ายบิล เช่น "ขอบคุณครับ"
    val billingDay: Int = 1,                // วันที่ออกบิลในแต่ละเดือน
    val dueDay: Int = 15,                   // วันครบกำหนดชำระ
    val lateFeeType: LateFeeType = LateFeeType.NONE,
    val lateFeeAmount: Long = 0L,           // ค่าปรับคงที่ (สตางค์)
    val lateFeeDailyAmount: Long = 0L,      // ค่าปรับรายวัน (สตางค์/วัน)
    val includeCarriedBalance: Boolean = true, // รวมยอดค้างชำระจากเดือนก่อน
    val prorateRent: Boolean = false,       // คิดค่าเช่าตามวันเข้า-ออกกลางเดือน
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

enum class LateFeeType {
    NONE,       // ไม่มีค่าปรับ
    FIXED,      // ค่าปรับแบบคงที่
    PER_DAY     // ค่าปรับรายวัน
}
