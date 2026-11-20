/*
 * TenantRepository.kt — Repository สำหรับผู้เช่า
 */
package com.dormhub.app.data.repository

import com.dormhub.app.data.dao.TenantDao
import com.dormhub.app.data.entity.Tenant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface TenantRepository {
    fun getActiveTenantForRoom(roomId: String): Flow<Tenant?>
    suspend fun getActiveTenantForRoomOnce(roomId: String): Tenant?
    fun getAllActiveTenants(): Flow<List<Tenant>>
    fun getTenantById(id: String): Flow<Tenant?>
    suspend fun getTenantByIdOnce(id: String): Tenant?
    fun getContractsEndingSoon(daysAhead: Int = 30): Flow<List<Tenant>>
    suspend fun saveTenant(tenant: Tenant)
    suspend fun deactivateTenant(id: String)
}

@Singleton
class TenantRepositoryImpl @Inject constructor(
    private val dao: TenantDao
) : TenantRepository {

    override fun getActiveTenantForRoom(roomId: String): Flow<Tenant?> =
        dao.getActiveTenantForRoom(roomId)

    override suspend fun getActiveTenantForRoomOnce(roomId: String): Tenant? =
        dao.getActiveTenantForRoomOnce(roomId)

    override fun getAllActiveTenants(): Flow<List<Tenant>> = dao.getAllActiveTenants()

    override fun getTenantById(id: String): Flow<Tenant?> = dao.getTenantById(id)

    override suspend fun getTenantByIdOnce(id: String): Tenant? = dao.getTenantByIdOnce(id)

    override fun getContractsEndingSoon(daysAhead: Int): Flow<List<Tenant>> {
        val deadline = System.currentTimeMillis() + (daysAhead.toLong() * 24 * 60 * 60 * 1000)
        return dao.getContractsEndingSoon(deadline)
    }

    override suspend fun saveTenant(tenant: Tenant) {
        dao.insert(tenant.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deactivateTenant(id: String) {
        dao.deactivate(id)
    }
}
