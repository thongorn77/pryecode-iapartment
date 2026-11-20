/*
 * build.gradle.kts (root) — Build Script หลักของโปรเจกต์
 * ============================================================
 * ประกาศ plugin ทั้งหมดที่ใช้ในโปรเจกต์ แต่ใช้ apply false
 * เพื่อให้ module :app เลือก apply เองได้
 */
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}
