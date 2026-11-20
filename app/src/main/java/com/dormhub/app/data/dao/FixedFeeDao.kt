/*
 * FixedFeeDao.kt — DAO สำหรับค่าบริการรายเดือนคงที่
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.FixedFee
import com.dormhub.app.data.entity.FixedFeeRoom
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedFeeDao {

    @Query("SELECT * FROM fixed_fees WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllFixedFees(): Flow<List<FixedFee>>

    @Query("SELECT * FROM fixed_fees WHERE isDeleted = 0 ORDER BY name ASC")
    suspend fun getAllFixedFeesOnce(): List<FixedFee>

    @Query("""
        SELECT ff.* FROM fixed_fees ff
        WHERE ff.isDeleted = 0 AND (
            ff.appliedToAll = 1
            OR EXISTS (SELECT 1 FROM fixed_fee_rooms ffr WHERE ffr.fixedFeeId = ff.id AND ffr.roomId = :roomId)
        )
    """)
    suspend fun getFeesForRoom(roomId: String): List<FixedFee>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fee: FixedFee)

    @Update
    suspend fun update(fee: FixedFee)

    @Query("UPDATE fixed_fees SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRoom(feeRoom: FixedFeeRoom)

    @Query("DELETE FROM fixed_fee_rooms WHERE fixedFeeId = :feeId")
    suspend fun deleteFeeRooms(feeId: String)
}
