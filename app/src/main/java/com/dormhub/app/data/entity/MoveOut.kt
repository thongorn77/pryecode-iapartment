/*
 * MoveOut.kt — บันทึกการย้ายออก
 * ============================================================
 * เมื่อผู้เช่าย้ายออก ระบบจะ:
 *   1. จดมิเตอร์ครั้งสุดท้าย
 *   2. คำนวณยอดค้างชำระ
 *   3. คำนวณคืนเงินมัดจำ (deposit - หักค่าเสียหาย - ยอดค้าง)
 * deductions = JSON string ของรายการหักออกจากเงินมัดจำ
 * เช่น [{"name":"ค่าซ่อม","amount":50000}]
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "move_outs")
data class MoveOut(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val tenantId: String,
    val roomId: String,
    val moveOutDate: Long,                      // epochMillis
    val finalElectricityReading: Long,          // หน่วย
    val finalWaterReading: Long,                // หน่วย
    val depositAmount: Long,                    // เงินประกันเดิม (สตางค์)
    val depositRefund: Long,                    // ยอดคืน (สตางค์)
    val deductions: String = "[]",              // JSON array
    val unpaidBalance: Long = 0L,               // สตางค์
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
