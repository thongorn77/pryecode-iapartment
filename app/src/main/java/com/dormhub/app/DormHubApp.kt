/*
 * DormHubApp.kt — Application Class (คลาสแอปพลิเคชันหลัก)
 * ============================================================
 * Android เรียก class นี้ก่อนสิ่งอื่นเมื่อแอปเปิดขึ้น
 * @HiltAndroidApp บอก Hilt ให้สร้าง dependency graph ทั้งหมด
 * ที่นี่จะ init WorkManager สำหรับ auto-backup (Phase 5)
 */
package com.dormhub.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DormHubApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
