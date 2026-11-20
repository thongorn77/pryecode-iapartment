/*
 * PropertyRepository.kt — Repository สำหรับตั้งค่าหอพัก
 * ============================================================
 * Repository เป็น layer กลางระหว่าง ViewModel และ DAO
 * ทำให้ ViewModel ไม่รู้ว่าข้อมูลมาจากไหน (DB / API / cache)
 * interface + implementation แยกกัน → test ง่าย (mock impl ได้)
 */
package com.dormhub.app.data.repository

import com.dormhub.app.data.dao.PropertySettingsDao
import com.dormhub.app.data.entity.PropertySettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface PropertyRepository {
    fun getSettings(): Flow<PropertySettings?>
    suspend fun getSettingsOnce(): PropertySettings?
    suspend fun saveSettings(settings: PropertySettings)
    suspend fun isOnboardingComplete(): Boolean
}

@Singleton
class PropertyRepositoryImpl @Inject constructor(
    private val dao: PropertySettingsDao
) : PropertyRepository {

    override fun getSettings(): Flow<PropertySettings?> = dao.getSettings()

    override suspend fun getSettingsOnce(): PropertySettings? = dao.getSettingsOnce()

    override suspend fun saveSettings(settings: PropertySettings) {
        dao.upsert(settings.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun isOnboardingComplete(): Boolean {
        val settings = dao.getSettingsOnce()
        return settings != null && settings.name.isNotBlank()
    }
}
