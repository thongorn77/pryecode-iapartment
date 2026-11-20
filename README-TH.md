# DormHub — คู่มือนักพัฒนา (ภาษาไทย)

## โครงสร้างโปรเจกต์

```
My Apartment/
├── settings.gradle.kts        ← กำหนด module ของโปรเจกต์
├── build.gradle.kts           ← plugin หลัก
├── gradle/libs.versions.toml  ← เวอร์ชัน library ทั้งหมด
└── app/
    ├── build.gradle.kts       ← dependency ของแอป
    └── src/main/
        ├── AndroidManifest.xml
        └── java/com/dormhub/app/
            ├── DormHubApp.kt           ← Application class
            ├── MainActivity.kt         ← Activity เดียว
            ├── data/
            │   ├── entity/             ← Room DB entities (14 ตาราง)
            │   ├── dao/                ← Data Access Objects
            │   ├── database/           ← AppDatabase + Converters
            │   └── repository/         ← Repository layer
            ├── di/                     ← Hilt DI modules
            └── ui/
                ├── theme/              ← Material 3 theme
                ├── navigation/         ← Nav graph
                ├── onboarding/         ← Wizard ตั้งค่าครั้งแรก
                ├── dashboard/          ← หน้าหลัก
                ├── rooms/              ← ห้องพัก
                ├── tenants/            ← ผู้เช่า
                └── settings/           ← ตั้งค่า
```

## วิธี Build และรันบนมือถือ

### ขั้นตอนที่ 1: เปิดโปรเจกต์ใน Android Studio
1. เปิด Android Studio
2. เลือก **File → Open**
3. เลือกโฟลเดอร์ `D:\Users Data\suriya\my_dev\My Apartment`
4. รอ Gradle sync เสร็จ (อาจใช้เวลา 2-5 นาทีครั้งแรก)

### ขั้นตอนที่ 2: เพิ่มไอคอนแอป
สร้างไอคอนที่ `app/src/main/res/mipmap-*/`:
- ใช้ **Image Asset Studio** ใน Android Studio: คลิกขวาที่ `res` → New → Image Asset

### ขั้นตอนที่ 3: รันบนมือถือ
1. เปิด **Developer Options** บนมือถือ
2. เปิด **USB Debugging**
3. เสียบสาย USB
4. กด **Run** (▶) ใน Android Studio

## การตั้งค่า Application ID

**สำคัญมาก**: ก่อน publish บน Google Play ต้องเปลี่ยน applicationId
ที่ `app/build.gradle.kts`:
```kotlin
applicationId = "com.dormhub.app"  // ← เปลี่ยนเป็นของคุณ เช่น "com.yourname.dormapp"
```
**ห้ามเปลี่ยนหลัง publish แล้ว** เพราะจะทำให้ผู้ใช้เก่าอัปเดตไม่ได้

## ขั้นตอนการออก Release Build

### 1. สร้าง Keystore
```
keytool -genkey -v -keystore dormhub-release.jks -alias dormhub -keyalg RSA -keysize 2048 -validity 10000
```
เก็บไฟล์ `.jks` และรหัสผ่านไว้ให้ดี ถ้าหายจะ publish อัปเดตไม่ได้

### 2. เพิ่ม signing config ใน app/build.gradle.kts
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("dormhub-release.jks")
            storePassword = "YOUR_STORE_PASSWORD"
            keyAlias = "dormhub"
            keyPassword = "YOUR_KEY_PASSWORD"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 3. Build AAB สำหรับ Play Console
**Build → Generate Signed Bundle / APK → Android App Bundle**

## Google Cloud Project (สำหรับ Drive Backup - Phase 5)

### ขั้นตอน:
1. ไปที่ https://console.cloud.google.com
2. สร้าง Project ใหม่ เช่น "DormHub App"
3. เปิดใช้ **Google Drive API**:
   - APIs & Services → Library → ค้นหา "Google Drive API" → Enable
4. สร้าง **OAuth 2.0 Credentials**:
   - APIs & Services → Credentials → Create Credentials → OAuth client ID
   - Application type: **Android**
   - Package name: `com.dormhub.app`
   - SHA-1: ดูจาก Android Studio → Gradle → app → Tasks → android → signingReport
5. สำหรับ Play App Signing:
   - Play Console → App → Setup → App signing → ดู SHA-1 จาก "App signing key certificate"
   - เพิ่ม SHA-1 นี้ใน Google Cloud Console ด้วย

### OAuth Consent Screen:
- User Type: **External**
- App name: DormHub
- Scopes: `https://www.googleapis.com/auth/drive.file` (ไม่ต้องผ่าน verification)

## เวอร์ชัน Library

เวอร์ชันทั้งหมดอยู่ใน `gradle/libs.versions.toml`
ตรวจสอบ update ได้ที่: Android Studio → File → Project Structure → Suggestions

## Phase ต่อไป
- **Phase 2**: จดมิเตอร์ + ออกบิล + บันทึกชำระเงิน
- **Phase 3**: สร้างรูปบิล PNG + QR PromptPay + ส่งผ่าน LINE
- **Phase 4**: รายงาน + กราฟ + ส่งออก CSV
- **Phase 5**: Backup/Restore Google Drive
- **Phase 6**: Play Billing + Localization + Demo Data
