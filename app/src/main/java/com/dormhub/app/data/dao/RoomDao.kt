/*
 * RoomDao.kt — DAO สำหรับห้องพัก
 * ============================================================
 * getRoomsByFloor() ใช้ใน Rooms screen จัดกลุ่มตามชั้น
 * getRoomCountByStatus() ใช้ใน Dashboard แสดงสถิติ
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.Room
import com.dormhub.app.data.entity.RoomStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {

    @Query("SELECT * FROM rooms WHERE isDeleted = 0 ORDER BY buildingId, floorId, number ASC")
    fun getAllRooms(): Flow<List<Room>>

    @Query("SELECT * FROM rooms WHERE isDeleted = 0 ORDER BY buildingId, floorId, number ASC")
    suspend fun getAllRoomsOnce(): List<Room>

    @Query("SELECT * FROM rooms WHERE floorId = :floorId AND isDeleted = 0 ORDER BY number ASC")
    fun getRoomsByFloor(floorId: String): Flow<List<Room>>

    @Query("SELECT * FROM rooms WHERE floorId = :floorId AND isDeleted = 0 ORDER BY number ASC")
    suspend fun getRoomsByFloorOnce(floorId: String): List<Room>

    @Query("SELECT * FROM rooms WHERE id = :id AND isDeleted = 0")
    fun getRoomById(id: String): Flow<Room?>

    @Query("SELECT * FROM rooms WHERE id = :id AND isDeleted = 0")
    suspend fun getRoomByIdOnce(id: String): Room?

    @Query("SELECT COUNT(*) FROM rooms WHERE status = :status AND isDeleted = 0")
    fun getRoomCountByStatus(status: RoomStatus): Flow<Int>

    @Query("SELECT COUNT(*) FROM rooms WHERE isDeleted = 0")
    fun getTotalRoomCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(room: Room)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rooms: List<Room>)

    @Update
    suspend fun update(room: Room)

    @Query("UPDATE rooms SET status = :status, currentTenantId = :tenantId, updatedAt = :now WHERE id = :id")
    suspend fun updateStatusAndTenant(id: String, status: RoomStatus, tenantId: String?, now: Long = System.currentTimeMillis())

    @Query("UPDATE rooms SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())
}
