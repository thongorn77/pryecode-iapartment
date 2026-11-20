# proguard-rules.pro — กฎการ minify โค้ดสำหรับ release build
# ProGuard จะ shrink + obfuscate โค้ดเพื่อลดขนาด APK

# Room — เก็บ entity class ไว้ (ถ้า minify ชื่อ field Room จะหาไม่เจอ)
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Kotlin serialization (ถ้าใช้ในอนาคต)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ZXing
-keep class com.google.zxing.** { *; }

# Google Play Billing
-keep class com.android.billingclient.** { *; }
