/*
 * Payment.kt — การชำระเงิน
 * ============================================================
 * บิลหนึ่งใบอาจถูกชำระหลายครั้ง (partial payment)
 * receiptNumber = รหัสใบเสร็จ เช่น "REC-2501-001"
 * method = วิธีชำระ: เงินสด / โอนเงิน / PromptPay
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val billId: String,
    val amount: Long,                               // สตางค์
    val paymentDate: Long,                          // epochMillis
    val method: PaymentMethod = PaymentMethod.CASH,
    val note: String = "",
    val receiptNumber: String,                      // REC-YYMM-001
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

enum class PaymentMethod {
    CASH,       // เงินสด
    TRANSFER,   // โอนเงิน
    PROMPTPAY   // PromptPay
}
