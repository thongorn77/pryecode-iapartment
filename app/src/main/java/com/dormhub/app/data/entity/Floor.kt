/*
 * Floor.kt — ชั้น (ของอาคาร)
 * ============================================================
 * แต่ละอาคารมีหลายชั้น เช่น ชั้น 1, ชั้น 2
 * number = เลขชั้น (1, 2, 3...)
 * name   = ชื่อเสริม (ถ้ามี) เช่น "ชั้นดาดฟ้า"
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "floors")
data class Floor(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val buildingId: String,
    val number: Int,
    val name: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
