/*
 * RoomType.kt — ประเภทห้องพัก
 * ============================================================
 * เช่น "ห้องเดี่ยว", "ห้องคู่", "ห้อง Studio"
 * defaultRent ใช้เป็นค่าเช่าเริ่มต้นเมื่อสร้างห้องใหม่
 * เปลี่ยน defaultRent ที่นี่ → กระทบห้องใหม่เท่านั้น (ห้องเก่าไม่เปลี่ยน)
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "room_types")
data class RoomType(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val defaultRent: Long = 0L,     // สตางค์
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
