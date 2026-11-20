/*
 * Converters.kt — TypeConverters สำหรับ Room
 * ============================================================
 * Room เก็บข้อมูลได้เฉพาะ primitive types (Long, Int, String, Boolean)
 * TypeConverter แปลง enum ↔ String เพื่อให้ Room เก็บได้
 * @TypeConverters(Converters::class) ใน AppDatabase จะ register ไฟล์นี้
 */
package com.dormhub.app.data.database

import androidx.room.TypeConverter
import com.dormhub.app.data.entity.BillLanguage
import com.dormhub.app.data.entity.BillStatus
import com.dormhub.app.data.entity.LateFeeType
import com.dormhub.app.data.entity.PaymentMethod
import com.dormhub.app.data.entity.RoomStatus

class Converters {

    @TypeConverter fun fromRoomStatus(v: RoomStatus): String = v.name
    @TypeConverter fun toRoomStatus(v: String): RoomStatus = RoomStatus.valueOf(v)

    @TypeConverter fun fromBillStatus(v: BillStatus): String = v.name
    @TypeConverter fun toBillStatus(v: String): BillStatus = BillStatus.valueOf(v)

    @TypeConverter fun fromPaymentMethod(v: PaymentMethod): String = v.name
    @TypeConverter fun toPaymentMethod(v: String): PaymentMethod = PaymentMethod.valueOf(v)

    @TypeConverter fun fromBillLanguage(v: BillLanguage): String = v.name
    @TypeConverter fun toBillLanguage(v: String): BillLanguage = BillLanguage.valueOf(v)

    @TypeConverter fun fromLateFeeType(v: LateFeeType): String = v.name
    @TypeConverter fun toLateFeeType(v: String): LateFeeType = LateFeeType.valueOf(v)
}
