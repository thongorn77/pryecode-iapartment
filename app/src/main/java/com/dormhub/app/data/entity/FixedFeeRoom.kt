/*
 * FixedFeeRoom.kt — ตาราง Junction สำหรับค่าบริการที่เก็บบางห้อง
 * ============================================================
 * ใช้เมื่อ FixedFee.appliedToAll = false
 * composite primary key (fixedFeeId + roomId) ป้องกันซ้ำ
 */
package com.dormhub.app.data.entity

import androidx.room.Entity

@Entity(
    tableName = "fixed_fee_rooms",
    primaryKeys = ["fixedFeeId", "roomId"]
)
data class FixedFeeRoom(
    val fixedFeeId: String,
    val roomId: String,
    val createdAt: Long = System.currentTimeMillis()
)
