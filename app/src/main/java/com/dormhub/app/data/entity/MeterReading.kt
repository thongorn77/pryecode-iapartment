/*
 * MeterReading.kt — การจดมิเตอร์ไฟฟ้าและน้ำ
 * ============================================================
 * บันทึกทุกครั้งที่จดมิเตอร์รายเดือน
 * previous/current = ค่ามิเตอร์ก่อน/หลัง (หน่วย ไม่ใช่สตางค์)
 * Override fields ใช้เมื่อมิเตอร์ถูกเปลี่ยนหรือ rollover (เลขล้น)
 * → ใส่ค่า previousOverride ด้วยตนเอง + บันทึกหมายเหตุ
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "meter_readings",
    indices = [Index(value = ["roomId", "yearMonth"], unique = true)]
)
data class MeterReading(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val roomId: String,
    val yearMonth: Int,                             // YYYYMM
    val electricityPrevious: Long,                  // หน่วย
    val electricityCurrent: Long,                   // หน่วย
    val waterPrevious: Long,                        // หน่วย
    val waterCurrent: Long,                         // หน่วย
    val electricityPreviousOverride: Long? = null,  // override กรณีมิเตอร์เปลี่ยน
    val electricityOverrideNote: String? = null,
    val waterPreviousOverride: Long? = null,
    val waterOverrideNote: String? = null,
    val photoPath: String? = null,                  // รูปถ่ายมิเตอร์
    val numberOfPeople: Int = 1,                    // สำหรับคิดค่าน้ำแบบเหมา
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    /** ค่ามิเตอร์ก่อนหน้าที่ใช้จริง (ใช้ override ถ้ามี) */
    val effectiveElectricityPrevious: Long
        get() = electricityPreviousOverride ?: electricityPrevious

    val effectiveWaterPrevious: Long
        get() = waterPreviousOverride ?: waterPrevious

    val electricityUnits: Long
        get() = maxOf(0L, electricityCurrent - effectiveElectricityPrevious)

    val waterUnits: Long
        get() = maxOf(0L, waterCurrent - effectiveWaterPrevious)
}
