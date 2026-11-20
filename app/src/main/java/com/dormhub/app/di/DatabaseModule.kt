/*
 * DatabaseModule.kt — Hilt Module สำหรับ Database
 * ============================================================
 * @Module บอก Hilt ว่าไฟล์นี้มีวิธีสร้าง dependency
 * @InstallIn(SingletonComponent) = มีชีวิตอยู่ตลอดอายุแอป (Singleton)
 * @Provides + @Singleton = สร้างครั้งเดียว ใช้ซ้ำตลอด
 * Hilt จะ inject dependency เหล่านี้ให้อัตโนมัติ
 */
package com.dormhub.app.di

import android.content.Context
import androidx.room.Room
import com.dormhub.app.data.dao.*
import com.dormhub.app.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "dormhub.db")
            .build()

    @Provides fun providePropertySettingsDao(db: AppDatabase): PropertySettingsDao = db.propertySettingsDao()
    @Provides fun provideBuildingDao(db: AppDatabase): BuildingDao = db.buildingDao()
    @Provides fun provideFloorDao(db: AppDatabase): FloorDao = db.floorDao()
    @Provides fun provideRoomTypeDao(db: AppDatabase): RoomTypeDao = db.roomTypeDao()
    @Provides fun provideRoomDao(db: AppDatabase): RoomDao = db.roomDao()
    @Provides fun provideTenantDao(db: AppDatabase): TenantDao = db.tenantDao()
    @Provides fun provideRateVersionDao(db: AppDatabase): RateVersionDao = db.rateVersionDao()
    @Provides fun provideFixedFeeDao(db: AppDatabase): FixedFeeDao = db.fixedFeeDao()
    @Provides fun provideMeterReadingDao(db: AppDatabase): MeterReadingDao = db.meterReadingDao()
    @Provides fun provideBillDao(db: AppDatabase): BillDao = db.billDao()
    @Provides fun providePaymentDao(db: AppDatabase): PaymentDao = db.paymentDao()
}
