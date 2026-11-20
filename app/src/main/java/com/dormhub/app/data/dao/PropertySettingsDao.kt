/*
 * PropertySettingsDao.kt — DAO สำหรับตั้งค่าหอพัก
 * ============================================================
 * DAO (Data Access Object) คือ interface ที่ Room ใช้สร้าง
 * SQL queries ให้อัตโนมัติตาม annotation ที่เราประกาศ
 * Flow<T> = ข้อมูล reactive → UI จะ update อัตโนมัติเมื่อข้อมูลเปลี่ยน
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.PropertySettings
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertySettingsDao {

    @Query("SELECT * FROM property_settings WHERE id = 'default' LIMIT 1")
    fun getSettings(): Flow<PropertySettings?>

    @Query("SELECT * FROM property_settings WHERE id = 'default' LIMIT 1")
    suspend fun getSettingsOnce(): PropertySettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: PropertySettings)
}
