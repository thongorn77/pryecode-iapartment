/*
 * RateVersionDao.kt — DAO สำหรับอัตราค่าไฟ-น้ำ
 * ============================================================
 * getRateForMonth() ใช้ตอนออกบิล — หาอัตราที่มีผลสำหรับเดือนนั้น
 * logic: หาอัตราที่ effectiveYearMonth <= เดือนที่ต้องการ
 *         เรียงจากใหม่ไปเก่า เอาแถวแรก
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.RateVersion
import kotlinx.coroutines.flow.Flow

@Dao
interface RateVersionDao {

    @Query("SELECT * FROM rate_versions WHERE isDeleted = 0 ORDER BY effectiveYearMonth DESC")
    fun getAllRates(): Flow<List<RateVersion>>

    @Query("""
        SELECT * FROM rate_versions
        WHERE isDeleted = 0 AND effectiveYearMonth <= :yearMonth
        ORDER BY effectiveYearMonth DESC
        LIMIT 1
    """)
    suspend fun getRateForMonth(yearMonth: Int): RateVersion?

    @Query("SELECT * FROM rate_versions WHERE isDeleted = 0 ORDER BY effectiveYearMonth DESC LIMIT 1")
    fun getLatestRate(): Flow<RateVersion?>

    @Query("SELECT * FROM rate_versions WHERE isDeleted = 0 ORDER BY effectiveYearMonth DESC LIMIT 1")
    suspend fun getLatestRateOnce(): RateVersion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rate: RateVersion)

    @Update
    suspend fun update(rate: RateVersion)
}
