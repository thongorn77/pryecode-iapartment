/*
 * Building.kt — อาคาร
 * ============================================================
 * หอพักหนึ่งอาจมีหลายอาคาร เช่น อาคาร A, อาคาร B
 * UUID เป็น PrimaryKey เพื่อรองรับ sync ในอนาคต
 * isDeleted = soft delete (ไม่ลบจริงจาก DB เพื่อ audit trail)
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "buildings")
data class Building(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
