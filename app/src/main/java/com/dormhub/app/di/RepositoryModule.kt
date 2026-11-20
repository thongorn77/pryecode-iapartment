/*
 * RepositoryModule.kt — Hilt Module สำหรับ Repository
 * ============================================================
 * @Binds บอก Hilt ว่า interface ใดให้ใช้ implementation ใด
 * เมื่อ ViewModel ขอ PropertyRepository → Hilt inject PropertyRepositoryImpl
 * abstract class + @Binds มีประสิทธิภาพกว่า @Provides
 */
package com.dormhub.app.di

import com.dormhub.app.data.repository.*
import com.dormhub.app.data.repository.BillRepository
import com.dormhub.app.data.repository.BillRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindPropertyRepository(impl: PropertyRepositoryImpl): PropertyRepository

    @Binds @Singleton
    abstract fun bindBuildingRepository(impl: BuildingRepositoryImpl): BuildingRepository

    @Binds @Singleton
    abstract fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository

    @Binds @Singleton
    abstract fun bindTenantRepository(impl: TenantRepositoryImpl): TenantRepository

    @Binds @Singleton
    abstract fun bindRateRepository(impl: RateRepositoryImpl): RateRepository

    @Binds @Singleton
    abstract fun bindBillRepository(impl: BillRepositoryImpl): BillRepository
}
