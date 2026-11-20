/*
 * settings.gradle.kts — การตั้งค่าโปรเจกต์ Gradle
 * ============================================================
 * บอก Gradle ว่าโปรเจกต์นี้มี module อะไรบ้าง (ในที่นี้มีแค่ :app)
 * และดาวน์โหลด plugin / library จาก repository ไหน
 */
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DormHub"
include(":app")
