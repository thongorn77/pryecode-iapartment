/*
 * app/build.gradle.kts — Build Script ของ app module
 * ============================================================
 * กำหนดค่าการ build แอปทั้งหมด เช่น
 *   applicationId  = ชื่อ package บน Google Play (ห้ามเปลี่ยนหลัง publish)
 *   minSdk/targetSdk = เวอร์ชัน Android ที่รองรับ
 *   dependencies   = library ทั้งหมดที่แอปต้องการ
 * plugins ที่ใช้:
 *   - android.application  = สร้าง APK/AAB
 *   - kotlin.android        = ใช้ Kotlin
 *   - kotlin.compose        = เปิดใช้ Jetpack Compose
 *   - hilt                 = Dependency Injection
 *   - ksp                  = Kotlin Symbol Processing (แทน KAPT)
 */
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// อ่าน keystore.properties (ถ้ามี — ไม่มีจะ skip signing)
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) load(keystorePropsFile.inputStream())
}

android {
    namespace        = "com.dormhub.app"
    compileSdk       = 35
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId            = "com.dormhub.app"
        minSdk                   = 26
        targetSdk                = 35
        versionCode              = 3
        versionName              = "1.3.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (keystorePropsFile.exists()) {
        signingConfigs {
            create("release") {
                storeFile     = file(keystoreProps["storeFile"] as String)
                storePassword = keystoreProps["storePassword"] as String
                keyAlias      = keystoreProps["keyAlias"] as String
                keyPassword   = keystoreProps["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropsFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            buildConfigField("Boolean", "IS_DEBUG_PRO_UNLOCKED", "false")
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable        = true
            buildConfigField("Boolean", "IS_DEBUG_PRO_UNLOCKED", "true")
        }
    }

    compileOptions {
        sourceCompatibility         = JavaVersion.VERSION_17
        targetCompatibility         = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }
}

// บอก Room ว่าให้ export schema (โครงสร้าง DB) ไว้ที่ไหน เพื่อตรวจสอบ migration
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Android Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Jetpack Compose (ใช้ BOM เพื่อให้เวอร์ชันสอดคล้องกัน)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Navigation
    implementation(libs.navigation.compose)

    // Room — ฐานข้อมูล SQLite แบบ offline
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Hilt — Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // WorkManager + Hilt-Work — งาน background (Phase 5: auto backup)
    implementation(libs.workmanager.ktx)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler.androidx)

    // Coroutines — async programming
    implementation(libs.kotlinx.coroutines.android)

    // DataStore — เก็บ preference เล็กๆ (เช่น onboarding complete flag)
    implementation(libs.datastore.preferences)

    // Coil — โหลดและแสดงรูปภาพ (เช่น logo หอพัก)
    implementation(libs.coil.compose)

    // ZXing — สร้าง QR Code สำหรับ PromptPay (Phase 3)
    implementation(libs.zxing.core)

    // Google Auth + Drive (Phase 5)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.google.id)

    // Google Play Billing (Phase 6)
    implementation(libs.play.billing)

    // Desugaring — ใช้ java.time API บน Android API 26+
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
