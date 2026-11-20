/*
 * AppDatabase.kt — ฐานข้อมูล SQLite หลักของแอป
 * ============================================================
 * Room สร้าง implementation ของ class นี้ให้อัตโนมัติตอน compile
 * @Database ต้องลิสต์ entity ทุกตัวและ version ปัจจุบัน
 * version = 1 สำหรับ release แรก
 * migration ที่ถูกต้องต้องถูกเพิ่มเมื่อ version เพิ่มขึ้น
 * exportSchema = true → Room บันทึก schema เป็น JSON ไว้ที่ app/schemas/
 *
 * Hilt จัดการ lifecycle ของ instance นี้ผ่าน DatabaseModule
 */
package com.dormhub.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dormhub.app.data.dao.*
import com.dormhub.app.data.entity.*

@Database(
    entities = [
        PropertySettings::class,
        Building::class,
        Floor::class,
        RoomType::class,
        Room::class,
        Tenant::class,
        RateVersion::class,
        FixedFee::class,
        FixedFeeRoom::class,
        MeterReading::class,
        Bill::class,
        BillItem::class,
        Payment::class,
        MoveOut::class,
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propertySettingsDao(): PropertySettingsDao
    abstract fun buildingDao(): BuildingDao
    abstract fun floorDao(): FloorDao
    abstract fun roomTypeDao(): RoomTypeDao
    abstract fun roomDao(): RoomDao
    abstract fun tenantDao(): TenantDao
    abstract fun rateVersionDao(): RateVersionDao
    abstract fun fixedFeeDao(): FixedFeeDao
    abstract fun meterReadingDao(): MeterReadingDao
    abstract fun billDao(): BillDao
    abstract fun paymentDao(): PaymentDao
}
