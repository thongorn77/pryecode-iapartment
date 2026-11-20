/*
 * Tenant.kt — ผู้เช่า
 * ============================================================
 * เก็บข้อมูลผู้เช่าทั้งปัจจุบันและอดีต (isActive = false)
 * billLanguage = ภาษาบิลสำหรับผู้เช่าคนนี้ (ไทย หรือ อังกฤษ)
 * deposit / advanceRent = เงินประกัน / ค่าเช่าล่วงหน้า (สตางค์)
 * nationalId = เลขบัตรประชาชน (ไม่บังคับ, เก็บ masked เพื่อ PDPA)
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val roomId: String,
    val name: String,
    val phone: String = "",
    val lineName: String = "",
    val moveInDate: Long,                        // epochMillis
    val contractStart: Long,
    val contractEnd: Long? = null,
    val deposit: Long = 0L,                      // สตางค์
    val advanceRent: Long = 0L,                  // สตางค์
    val billLanguage: BillLanguage = BillLanguage.TH,
    val nationalId: String? = null,              // เลขบัตร (optional)
    val note: String = "",
    val isActive: Boolean = true,                // false = ย้ายออกแล้ว
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

enum class BillLanguage {
    TH,   // ภาษาไทย
    EN    // ภาษาอังกฤษ
}
