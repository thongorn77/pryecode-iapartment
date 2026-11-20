/*
 * BillDao.kt — DAO สำหรับบิล
 * ============================================================
 * getUnpaidBills() ใช้ใน Outstanding screen
 * getBillSummaryForMonth() ใช้ใน Dashboard
 */
package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.Bill
import com.dormhub.app.data.entity.BillStatus
import kotlinx.coroutines.flow.Flow

data class MonthSummary(
    val yearMonth: Int,
    val totalBilled: Long,
    val totalPaid: Long,
)

@Dao
interface BillDao {

    @Query("SELECT * FROM bills WHERE yearMonth = :yearMonth AND isDeleted = 0 ORDER BY roomNumber ASC")
    fun getBillsForMonth(yearMonth: Int): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE yearMonth = :yearMonth AND isDeleted = 0 ORDER BY roomNumber ASC")
    suspend fun getBillsForMonthOnce(yearMonth: Int): List<Bill>

    @Query("SELECT * FROM bills WHERE id = :id AND isDeleted = 0")
    fun getBillById(id: String): Flow<Bill?>

    @Query("SELECT * FROM bills WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getBillByIdOnce(id: String): Bill?

    @Query("SELECT * FROM bills WHERE roomId = :roomId AND isDeleted = 0 ORDER BY yearMonth DESC")
    fun getBillsForRoom(roomId: String): Flow<List<Bill>>

    @Query("""
        SELECT * FROM bills
        WHERE status IN ('ISSUED','PARTIAL') AND isDeleted = 0
        ORDER BY dueDate ASC, total DESC
    """)
    fun getUnpaidBills(): Flow<List<Bill>>

    @Query("SELECT MAX(billNumber) FROM bills WHERE billNumber LIKE :prefix || '%'")
    suspend fun getMaxBillNumber(prefix: String): String?

    @Query("SELECT COUNT(*) FROM bills WHERE yearMonth = :yearMonth AND status = :status AND isDeleted = 0")
    fun getBillCountByStatus(yearMonth: Int, status: BillStatus): Flow<Int>

    @Query("SELECT SUM(total) FROM bills WHERE yearMonth = :yearMonth AND status != 'VOID' AND isDeleted = 0")
    fun getTotalBilledForMonth(yearMonth: Int): Flow<Long?>

    @Query("SELECT SUM(paidAmount) FROM bills WHERE yearMonth = :yearMonth AND isDeleted = 0")
    fun getTotalPaidForMonth(yearMonth: Int): Flow<Long?>

    @Query("""
        SELECT yearMonth,
               COALESCE(SUM(CASE WHEN status != 'VOID' THEN total ELSE 0 END), 0) AS totalBilled,
               COALESCE(SUM(paidAmount), 0) AS totalPaid
        FROM bills
        WHERE isDeleted = 0
        GROUP BY yearMonth
        ORDER BY yearMonth DESC
        LIMIT 24
    """)
    fun getMonthlyReport(): Flow<List<MonthSummary>>

    @Query("SELECT * FROM bills WHERE roomId = :roomId AND isDeleted = 0 ORDER BY yearMonth DESC")
    suspend fun getBillsForRoomOnce(roomId: String): List<Bill>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: Bill)

    @Update
    suspend fun update(bill: Bill)
}
