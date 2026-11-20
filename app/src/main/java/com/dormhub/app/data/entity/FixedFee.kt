/*
 * FixedFee.kt — ค่าบริการรายเดือนคงที่
 * ============================================================
 * เช่น ค่าอินเทอร์เน็ต 200 บาท, ค่าขยะ 50 บาท
 * appliedToAll = true  → เก็บทุกห้อง
 * appliedToAll = false → ดูตาราง FixedFeeRoom ว่าเก็บห้องไหนบ้าง
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "fixed_fees")
data class FixedFee(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Long,                   // สตางค์
    val appliedToAll: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
