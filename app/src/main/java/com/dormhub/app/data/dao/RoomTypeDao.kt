/*
 * RoomTypeDao.kt — DAO สำหรับประเภทห้องพัก
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.RoomType
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomTypeDao {

    @Query("SELECT * FROM room_types WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllRoomTypes(): Flow<List<RoomType>>

    @Query("SELECT * FROM room_types WHERE isDeleted = 0 ORDER BY name ASC")
    suspend fun getAllRoomTypesOnce(): List<RoomType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(roomType: RoomType)

    @Update
    suspend fun update(roomType: RoomType)

    @Query("UPDATE room_types SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())
}
