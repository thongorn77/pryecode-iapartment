/*
 * PaymentDao.kt — DAO สำหรับการชำระเงิน
 * ============================================================
 * getLatestReceiptNumber() ใช้สร้างเลขใบเสร็จ running number
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE billId = :billId AND isDeleted = 0 ORDER BY paymentDate DESC")
    fun getPaymentsForBill(billId: String): Flow<List<Payment>>

    @Query("SELECT MAX(receiptNumber) FROM payments WHERE receiptNumber LIKE :prefix || '%'")
    suspend fun getMaxReceiptNumber(prefix: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: Payment)

    @Query("UPDATE payments SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long = System.currentTimeMillis())
}
