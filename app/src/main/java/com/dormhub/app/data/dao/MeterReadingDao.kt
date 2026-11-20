/*
 * MeterReadingDao.kt — DAO สำหรับการจดมิเตอร์
 * ============================================================
 * getLatestReadingForRoom() ใช้ดึงค่ามิเตอร์ล่าสุดมา pre-fill
 *   ใน Meter Entry screen (ค่า current กลายเป็น previous เดือนถัดไป)
 * getReadingsForMonth() ใช้ตอนออกบิล
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.MeterReading
import kotlinx.coroutines.flow.Flow

@Dao
interface MeterReadingDao {

    @Query("SELECT * FROM meter_readings WHERE roomId = :roomId AND yearMonth = :yearMonth AND isDeleted = 0 LIMIT 1")
    fun getReadingForRoomMonth(roomId: String, yearMonth: Int): Flow<MeterReading?>

    @Query("SELECT * FROM meter_readings WHERE roomId = :roomId AND yearMonth = :yearMonth AND isDeleted = 0 LIMIT 1")
    suspend fun getReadingForRoomMonthOnce(roomId: String, yearMonth: Int): MeterReading?

    @Query("""
        SELECT * FROM meter_readings
        WHERE roomId = :roomId AND isDeleted = 0
        ORDER BY yearMonth DESC
        LIMIT 1
    """)
    suspend fun getLatestReadingForRoom(roomId: String): MeterReading?

    @Query("SELECT * FROM meter_readings WHERE yearMonth = :yearMonth AND isDeleted = 0")
    fun getReadingsForMonth(yearMonth: Int): Flow<List<MeterReading>>

    @Query("SELECT * FROM meter_readings WHERE yearMonth = :yearMonth AND isDeleted = 0")
    suspend fun getReadingsForMonthOnce(yearMonth: Int): List<MeterReading>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reading: MeterReading)

    @Update
    suspend fun update(reading: MeterReading)
}
