import java.util.zip.ZipFile

buildscript {
    extra.apply {
        set("compose_version", "1.3.1")
    }
}// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.4.0" apply false
    id("com.android.library") version "7.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.7.20" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}

tasks.register("loadBackup") {
    val backupDir = System.getenv("APPS_BACKUP_DIR")
    val backupZipFilePath = backupDir + rootProject.name.lowercase() + "_database.zip"
    val rawDirPath = "app/src/main/res/raw"
    ZipFile(backupZipFilePath).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            if (entry.name != "room_master_table.csv") {
                zip.getInputStream(entry).use { input ->
                    File(rawDirPath, entry.name).outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
}
