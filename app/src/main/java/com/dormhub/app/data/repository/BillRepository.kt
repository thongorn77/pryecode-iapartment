/*
 * BillRepository.kt — Repository สำหรับบิลและการชำระเงิน
 * ============================================================
 * Phase 2: เพิ่ม generateBillsForMonth(), recordPayment(), voidBill()
 */
package com.dormhub.app.data.repository

import com.dormhub.app.data.dao.BillDao
import com.dormhub.app.data.dao.MeterReadingDao
import com.dormhub.app.data.dao.PaymentDao
import com.dormhub.app.data.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface BillRepository {
    fun getTotalBilledForMonth(yearMonth: Int): Flow<Long?>
    fun getTotalPaidForMonth(yearMonth: Int): Flow<Long?>
    fun getUnpaidBills(): Flow<List<Bill>>
    fun getBillsForRoom(roomId: String): Flow<List<Bill>>
    fun getBillsForMonth(yearMonth: Int): Flow<List<Bill>>
    fun getBillById(id: String): Flow<Bill?>
    fun getPaymentsForBill(billId: String): Flow<List<Payment>>
    suspend fun saveBill(bill: Bill)
    suspend fun savePayment(payment: Payment)
    suspend fun voidBill(billId: String, reason: String)
    suspend fun generateNextBillNumber(yearMonth: Int): String
    suspend fun generateNextReceiptNumber(yearMonth: Int): String
    suspend fun recordPayment(
        bill: Bill,
        amount: Long,
        method: PaymentMethod,
        note: String,
        paymentDate: Long
    )
}

@Singleton
class BillRepositoryImpl @Inject constructor(
    private val billDao: BillDao,
    private val paymentDao: PaymentDao,
    private val meterReadingDao: MeterReadingDao,
) : BillRepository {

    override fun getTotalBilledForMonth(yearMonth: Int): Flow<Long?> =
        billDao.getTotalBilledForMonth(yearMonth)

    override fun getTotalPaidForMonth(yearMonth: Int): Flow<Long?> =
        billDao.getTotalPaidForMonth(yearMonth)

    override fun getUnpaidBills(): Flow<List<Bill>> = billDao.getUnpaidBills()

    override fun getBillsForRoom(roomId: String): Flow<List<Bill>> =
        billDao.getBillsForRoom(roomId)

    override fun getBillsForMonth(yearMonth: Int): Flow<List<Bill>> =
        billDao.getBillsForMonth(yearMonth)

    override fun getBillById(id: String): Flow<Bill?> = billDao.getBillById(id)

    override fun getPaymentsForBill(billId: String): Flow<List<Payment>> =
        paymentDao.getPaymentsForBill(billId)

    override suspend fun saveBill(bill: Bill) {
        billDao.insert(bill.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun savePayment(payment: Payment) {
        paymentDao.insert(payment.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun voidBill(billId: String, reason: String) {
        val bill = billDao.getBillByIdOnce(billId) ?: return
        billDao.update(bill.copy(
            status = BillStatus.VOID,
            voidedAt = System.currentTimeMillis(),
            voidReason = reason,
            updatedAt = System.currentTimeMillis()
        ))
    }

    override suspend fun generateNextBillNumber(yearMonth: Int): String {
        val yy = (yearMonth / 100) % 100
        val mm = yearMonth % 100
        val prefix = "INV-%02d%02d".format(yy, mm)
        val last = billDao.getMaxBillNumber(prefix)
        val seq = if (last != null) {
            (last.substringAfterLast("-").toIntOrNull() ?: 0) + 1
        } else 1
        return "$prefix-%03d".format(seq)
    }

    override suspend fun generateNextReceiptNumber(yearMonth: Int): String {
        val yy = (yearMonth / 100) % 100
        val mm = yearMonth % 100
        val prefix = "REC-%02d%02d".format(yy, mm)
        val last = paymentDao.getMaxReceiptNumber(prefix)
        val seq = if (last != null) {
            (last.substringAfterLast("-").toIntOrNull() ?: 0) + 1
        } else 1
        return "$prefix-%03d".format(seq)
    }

    override suspend fun recordPayment(
        bill: Bill,
        amount: Long,
        method: PaymentMethod,
        note: String,
        paymentDate: Long
    ) {
        val receiptNumber = generateNextReceiptNumber(bill.yearMonth)
        paymentDao.insert(
            Payment(
                billId = bill.id,
                amount = amount,
                paymentDate = paymentDate,
                method = method,
                note = note,
                receiptNumber = receiptNumber,
            )
        )
        val newPaid = bill.paidAmount + amount
        val newStatus = when {
            newPaid >= bill.total -> BillStatus.PAID
            newPaid > 0L         -> BillStatus.PARTIAL
            else                 -> BillStatus.ISSUED
        }
        billDao.update(bill.copy(
            paidAmount = newPaid,
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        ))
    }
}
