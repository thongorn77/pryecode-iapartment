/*
 * RoomRepository.kt — Repository สำหรับห้องพักและประเภทห้อง
 */
package com.dormhub.app.data.repository

import com.dormhub.app.data.dao.RoomDao
import com.dormhub.app.data.dao.RoomTypeDao
import com.dormhub.app.data.entity.Room
import com.dormhub.app.data.entity.RoomStatus
import com.dormhub.app.data.entity.RoomType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface RoomRepository {
    fun getAllRooms(): Flow<List<Room>>
    suspend fun getAllRoomsOnce(): List<Room>
    fun getRoomById(id: String): Flow<Room?>
    suspend fun getRoomByIdOnce(id: String): Room?
    fun getAllRoomTypes(): Flow<List<RoomType>>
    suspend fun getAllRoomTypesOnce(): List<RoomType>
    fun getRoomCountByStatus(status: RoomStatus): Flow<Int>
    fun getTotalRoomCount(): Flow<Int>
    suspend fun saveRoom(room: Room)
    suspend fun saveRooms(rooms: List<Room>)
    suspend fun updateRoomStatus(id: String, status: RoomStatus, tenantId: String?)
    suspend fun deleteRoom(id: String)
    suspend fun saveRoomType(roomType: RoomType)
}

@Singleton
class RoomRepositoryImpl @Inject constructor(
    private val roomDao: RoomDao,
    private val roomTypeDao: RoomTypeDao
) : RoomRepository {

    override fun getAllRooms(): Flow<List<Room>> = roomDao.getAllRooms()
    override suspend fun getAllRoomsOnce(): List<Room> = roomDao.getAllRoomsOnce()
    override fun getRoomById(id: String): Flow<Room?> = roomDao.getRoomById(id)
    override suspend fun getRoomByIdOnce(id: String): Room? = roomDao.getRoomByIdOnce(id)
    override fun getAllRoomTypes(): Flow<List<RoomType>> = roomTypeDao.getAllRoomTypes()
    override suspend fun getAllRoomTypesOnce(): List<RoomType> = roomTypeDao.getAllRoomTypesOnce()
    override fun getRoomCountByStatus(status: RoomStatus): Flow<Int> = roomDao.getRoomCountByStatus(status)
    override fun getTotalRoomCount(): Flow<Int> = roomDao.getTotalRoomCount()

    override suspend fun saveRoom(room: Room) {
        roomDao.insert(room.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun saveRooms(rooms: List<Room>) {
        roomDao.insertAll(rooms)
    }

    override suspend fun updateRoomStatus(id: String, status: RoomStatus, tenantId: String?) {
        roomDao.updateStatusAndTenant(id, status, tenantId)
    }

    override suspend fun deleteRoom(id: String) {
        roomDao.softDelete(id)
    }

    override suspend fun saveRoomType(roomType: RoomType) {
        roomTypeDao.insert(roomType.copy(updatedAt = System.currentTimeMillis()))
    }
}
