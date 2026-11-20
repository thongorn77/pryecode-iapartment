/*
 * RateVersion.kt — อัตราค่าไฟ / ค่าน้ำ (Versioned ตามเดือน)
 * ============================================================
 * อัตราค่าไฟ-น้ำถูก version ตามเดือนที่มีผล (effectiveYearMonth)
 * เพื่อให้บิลเก่าไม่เปลี่ยนแปลงเมื่ออัปเดตอัตราใหม่
 * effectiveYearMonth = YYYYMM เช่น 202501 = มกราคม 2025
 *
 * ค่าน้ำสามารถเลือกได้ 2 แบบ:
 *   1. คิดตามมิเตอร์  → waterRatePerUnit ใช้งาน
 *   2. เหมาต่อคน     → waterFlatPerPerson ใช้งาน (ถ้ามีค่า)
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "rate_versions")
data class RateVersion(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val effectiveYearMonth: Int,            // YYYYMM
    val electricityRatePerUnit: Long,       // สตางค์ต่อหน่วย
    val electricityMinimum: Long,           // ค่าต่ำสุด (สตางค์)
    val waterRatePerUnit: Long,             // สตางค์ต่อหน่วย
    val waterMinimum: Long,                 // ค่าต่ำสุด (สตางค์)
    val waterFlatPerPerson: Long? = null,   // เหมาต่อคน (สตางค์) ถ้า null = คิดตามมิเตอร์
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
