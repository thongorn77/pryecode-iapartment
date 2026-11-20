/*
 * BuildingDao.kt — DAO สำหรับอาคาร
 * ============================================================
 * getAllBuildings() คืน Flow → UI อัปเดตอัตโนมัติเมื่อเพิ่ม/แก้/ลบอาคาร
 * softDelete() ไม่ได้ลบจริง แค่ set isDeleted = 1 (true)
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.Building
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {

    @Query("SELECT * FROM buildings WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllBuildings(): Flow<List<Building>>

    @Query("SELECT * FROM buildings WHERE id = :id AND isDeleted = 0")
    suspend fun getBuildingById(id: String): Building?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(building: Building)

    @Update
    suspend fun update(building: Building)

    @Query("UPDATE buildings SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())
}
