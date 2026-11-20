/*
 * TenantDao.kt — DAO สำหรับผู้เช่า
 * ============================================================
 * getActiveTenantForRoom() ใช้หาผู้เช่าปัจจุบันของแต่ละห้อง
 * getContractsEndingSoon() ใช้ใน Dashboard แสดงสัญญาที่ใกล้หมด
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.Tenant
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {

    @Query("SELECT * FROM tenants WHERE roomId = :roomId AND isActive = 1 AND isDeleted = 0 LIMIT 1")
    fun getActiveTenantForRoom(roomId: String): Flow<Tenant?>

    @Query("SELECT * FROM tenants WHERE roomId = :roomId AND isActive = 1 AND isDeleted = 0 LIMIT 1")
    suspend fun getActiveTenantForRoomOnce(roomId: String): Tenant?

    @Query("SELECT * FROM tenants WHERE isActive = 1 AND isDeleted = 0 ORDER BY name ASC")
    fun getAllActiveTenants(): Flow<List<Tenant>>

    @Query("SELECT * FROM tenants WHERE id = :id AND isDeleted = 0")
    fun getTenantById(id: String): Flow<Tenant?>

    @Query("SELECT * FROM tenants WHERE id = :id AND isDeleted = 0")
    suspend fun getTenantByIdOnce(id: String): Tenant?

    @Query("""
        SELECT * FROM tenants
        WHERE isActive = 1 AND isDeleted = 0
          AND contractEnd IS NOT NULL
          AND contractEnd <= :deadline
        ORDER BY contractEnd ASC
    """)
    fun getContractsEndingSoon(deadline: Long): Flow<List<Tenant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tenant: Tenant)

    @Update
    suspend fun update(tenant: Tenant)

    @Query("UPDATE tenants SET isActive = 0, updatedAt = :now WHERE id = :id")
    suspend fun deactivate(id: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE tenants SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())
}
