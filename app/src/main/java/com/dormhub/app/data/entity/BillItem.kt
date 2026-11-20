/*
 * BillItem.kt — รายการในบิล (ค่าบริการเพิ่มเติม / ลดหย่อน)
 * ============================================================
 * ใช้เก็บ:
 *   - ค่าบริการ fixed fee แต่ละรายการ (ค่าอินเทอร์เน็ต, ค่าขยะ)
 *   - รายการพิเศษที่เพิ่มเข้ามา เช่น "ค่าซ่อม 500 บาท"
 *   - ส่วนลด (amount เป็นลบ) เช่น "ลดพิเศษ -200 บาท"
 * sortOrder ใช้จัดลำดับการแสดงผลในบิล
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "bill_items")
data class BillItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val billId: String,
    val name: String,
    val amount: Long,       // สตางค์ (ติดลบได้ = ส่วนลด)
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
