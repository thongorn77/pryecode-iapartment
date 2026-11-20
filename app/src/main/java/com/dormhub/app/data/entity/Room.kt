/*
 * Room.kt — ห้องพัก
 * ============================================================
 * ห้องพักแต่ละห้อง อยู่ในชั้น (floorId) และอาคาร (buildingId)
 * monthlyRent = ค่าเช่าของห้องนี้โดยเฉพาะ (override จาก RoomType)
 * status      = สถานะห้อง: ว่าง / มีผู้เช่า / ซ่อมบำรุง
 * currentTenantId = id ของผู้เช่าปัจจุบัน (null ถ้าว่าง)
 *
 * หมายเหตุ: "Room" ใน Jetpack Compose คือชื่อ library ด้วย
 * → ตั้ง class ชื่อ "Room" ได้เพราะอยู่ในคนละ package
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "rooms")
data class Room(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val buildingId: String,
    val floorId: String,
    val roomTypeId: String? = null,
    val number: String,                 // เลขห้อง เช่น "101", "A-205"
    val monthlyRent: Long,              // สตางค์
    val status: RoomStatus = RoomStatus.VACANT,
    val currentTenantId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

enum class RoomStatus {
    VACANT,       // ว่าง
    OCCUPIED,     // มีผู้เช่า
    MAINTENANCE   // ซ่อมบำรุง
}
