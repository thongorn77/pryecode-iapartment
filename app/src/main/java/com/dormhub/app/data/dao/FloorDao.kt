/*
 * FloorDao.kt — DAO สำหรับชั้น
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.Floor
import kotlinx.coroutines.flow.Flow

@Dao
interface FloorDao {

    @Query("SELECT * FROM floors WHERE buildingId = :buildingId AND isDeleted = 0 ORDER BY number ASC")
    fun getFloorsForBuilding(buildingId: String): Flow<List<Floor>>

    @Query("SELECT * FROM floors WHERE buildingId = :buildingId AND isDeleted = 0 ORDER BY number ASC")
    suspend fun getFloorsForBuildingOnce(buildingId: String): List<Floor>

    @Query("SELECT * FROM floors WHERE isDeleted = 0 ORDER BY buildingId ASC, number ASC")
    suspend fun getAllFloorsOnce(): List<Floor>

    @Query("SELECT * FROM floors WHERE id = :id AND isDeleted = 0")
    suspend fun getFloorById(id: String): Floor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(floor: Floor)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(floors: List<Floor>)

    @Update
    suspend fun update(floor: Floor)

    @Query("UPDATE floors SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())
}
