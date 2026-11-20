package com.dormhub.app.data.dao

import androidx.room.*
import com.dormhub.app.data.entity.BillItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BillItemDao {

    @Query("SELECT * FROM bill_items WHERE billId = :billId ORDER BY sortOrder ASC, createdAt ASC")
    fun getItemsForBill(billId: String): Flow<List<BillItem>>

    @Query("SELECT * FROM bill_items WHERE billId = :billId ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getItemsForBillOnce(billId: String): List<BillItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: BillItem)

    @Query("DELETE FROM bill_items WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM bill_items WHERE billId = :billId")
    suspend fun deleteAllForBill(billId: String)
}
