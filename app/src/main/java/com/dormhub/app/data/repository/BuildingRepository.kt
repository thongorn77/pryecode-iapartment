/*
 * BuildingRepository.kt — Repository สำหรับอาคารและชั้น
 * ============================================================
 * จัดการ Building และ Floor ด้วยกัน เพราะมักใช้พร้อมกัน
 */
package com.dormhub.app.data.repository

import com.dormhub.app.data.dao.BuildingDao
import com.dormhub.app.data.dao.FloorDao
import com.dormhub.app.data.entity.Building
import com.dormhub.app.data.entity.Floor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface BuildingRepository {
    fun getAllBuildings(): Flow<List<Building>>
    fun getFloorsForBuilding(buildingId: String): Flow<List<Floor>>
    suspend fun getAllFloorsOnce(): List<Floor>
    suspend fun saveBuilding(building: Building)
    suspend fun saveFloor(floor: Floor)
    suspend fun saveFloors(floors: List<Floor>)
    suspend fun deleteBuilding(id: String)
    suspend fun deleteFloor(id: String)
}

@Singleton
class BuildingRepositoryImpl @Inject constructor(
    private val buildingDao: BuildingDao,
    private val floorDao: FloorDao
) : BuildingRepository {

    override fun getAllBuildings(): Flow<List<Building>> = buildingDao.getAllBuildings()

    override fun getFloorsForBuilding(buildingId: String): Flow<List<Floor>> =
        floorDao.getFloorsForBuilding(buildingId)

    override suspend fun getAllFloorsOnce(): List<Floor> = floorDao.getAllFloorsOnce()

    override suspend fun saveBuilding(building: Building) {
        buildingDao.insert(building.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun saveFloor(floor: Floor) {
        floorDao.insert(floor.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun saveFloors(floors: List<Floor>) {
        floorDao.insertAll(floors)
    }

    override suspend fun deleteBuilding(id: String) {
        buildingDao.softDelete(id)
    }

    override suspend fun deleteFloor(id: String) {
        floorDao.softDelete(id)
    }
}
