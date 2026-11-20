/*
 * Bill.kt — บิลค่าเช่า
 * ============================================================
 * บิลแต่ละใบเก็บ snapshot ข้อมูลผู้เช่า + ห้อง ณ เวลาออกบิล
 * เพื่อให้บิลเก่าไม่เปลี่ยนแปลงแม้จะแก้ข้อมูลในภายหลัง
 * billNumber = รหัสบิล เช่น "INV-2501-001"
 * status: DRAFT → ISSUED → PARTIAL/PAID, หรือ VOID
 * บิลที่ ISSUED แล้วจะแก้ไม่ได้ ต้อง void + reissue เท่านั้น
 */
package com.dormhub.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val billNumber: String,             // INV-YYMM-001
    val roomId: String,
    val tenantId: String,
    // Snapshot ข้อมูล ณ เวลาออกบิล
    val tenantName: String,
    val tenantPhone: String,
    val tenantLineName: String,
    val roomNumber: String,
    val buildingName: String,
    val yearMonth: Int,                 // YYYYMM
    val dueDate: Long,                  // epochMillis
    // ค่าเช่า
    val rent: Long,                     // สตางค์
    // มิเตอร์ไฟ
    val electricityPrevious: Long,
    val electricityCurrent: Long,
    val electricityUnits: Long,
    val electricityAmount: Long,        // สตางค์
    // มิเตอร์น้ำ
    val waterPrevious: Long,
    val waterCurrent: Long,
    val waterUnits: Long,
    val waterAmount: Long,              // สตางค์
    // ค่าบริการอื่นๆ
    val fixedFeesTotal: Long = 0L,     // สตางค์
    val extrasTotal: Long = 0L,        // สตางค์ (รายการเพิ่มเติม / ลดหย่อน)
    val previousBalance: Long = 0L,    // ยอดค้างจากเดือนก่อน (สตางค์)
    val lateFee: Long = 0L,            // ค่าปรับ (สตางค์)
    val total: Long,                   // ยอดรวม (สตางค์)
    val status: BillStatus = BillStatus.DRAFT,
    val paidAmount: Long = 0L,         // ยอดที่ชำระแล้ว (สตางค์)
    val voidedAt: Long? = null,
    val voidReason: String? = null,
    val reissuedFromBillId: String? = null, // บิลต้นฉบับก่อน void
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    val remainingAmount: Long
        get() = maxOf(0L, total - paidAmount)
}

enum class BillStatus {
    DRAFT,    // ร่าง ยังไม่ส่ง
    ISSUED,   // ออกบิลแล้ว
    PARTIAL,  // ชำระบางส่วน
    PAID,     // ชำระครบ
    VOID      // ยกเลิก
}
