/*
 * RateRepository.kt — Repository สำหรับอัตราค่าไฟ-น้ำ และค่าบริการคงที่
 */
package com.dormhub.app.data.repository

import com.dormhub.app.data.dao.FixedFeeDao
import com.dormhub.app.data.dao.RateVersionDao
import com.dormhub.app.data.entity.FixedFee
import com.dormhub.app.data.entity.RateVersion
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface RateRepository {
    fun getAllRates(): Flow<List<RateVersion>>
    fun getLatestRate(): Flow<RateVersion?>
    suspend fun getLatestRateOnce(): RateVersion?
    suspend fun getRateForMonth(yearMonth: Int): RateVersion?
    suspend fun saveRate(rate: RateVersion)
    fun getAllFixedFees(): Flow<List<FixedFee>>
    suspend fun getAllFixedFeesOnce(): List<FixedFee>
    suspend fun getFeesForRoom(roomId: String): List<FixedFee>
    suspend fun saveFixedFee(fee: FixedFee)
    suspend fun deleteFixedFee(id: String)
}

@Singleton
class RateRepositoryImpl @Inject constructor(
    private val rateDao: RateVersionDao,
    private val feeDao: FixedFeeDao
) : RateRepository {

    override fun getAllRates(): Flow<List<RateVersion>> = rateDao.getAllRates()
    override fun getLatestRate(): Flow<RateVersion?> = rateDao.getLatestRate()
    override suspend fun getLatestRateOnce(): RateVersion? = rateDao.getLatestRateOnce()
    override suspend fun getRateForMonth(yearMonth: Int): RateVersion? = rateDao.getRateForMonth(yearMonth)
    override suspend fun saveRate(rate: RateVersion) {
        rateDao.insert(rate.copy(updatedAt = System.currentTimeMillis()))
    }

    override fun getAllFixedFees(): Flow<List<FixedFee>> = feeDao.getAllFixedFees()
    override suspend fun getAllFixedFeesOnce(): List<FixedFee> = feeDao.getAllFixedFeesOnce()
    override suspend fun getFeesForRoom(roomId: String): List<FixedFee> = feeDao.getFeesForRoom(roomId)
    override suspend fun saveFixedFee(fee: FixedFee) {
        feeDao.insert(fee.copy(updatedAt = System.currentTimeMillis()))
    }
    override suspend fun deleteFixedFee(id: String) {
        feeDao.softDelete(id)
    }
}
