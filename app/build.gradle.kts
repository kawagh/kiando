@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.cookpad.android.plugin.license-tools") version "1.2.8"
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
}

fun convertVersionNameToCode(versionName: String): Int {
    val majorVersion = versionName.split('.')[0].toInt()
    val minorVersion = versionName.split('.')[1].toInt()
    val revision = versionName.split('.')[2].toInt()
    return 10000 * majorVersion + 100 * minorVersion + revision
}

val appVersion = "1.0.11"

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "jp.kawagh.kiando"
        minSdk = 21
        targetSdk = 33
        versionName = appVersion
        versionCode = convertVersionNameToCode(appVersion)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "jp.kawagh.kiando"
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    val compose_version: String by project
    implementation("androidx.compose.ui:ui:$compose_version")
    val material3_version = "1.0.1"
    implementation("androidx.compose.material3:material3:$material3_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_version")
    androidTestImplementation("androidx.test:core:1.5.0")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_version")

    val nav_version = "2.5.3"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    val room_version = "2.5.0"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-testing:$room_version")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.compose.material:material-icons-extended:$compose_version")
    // change system-ui style
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.25.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.7.0")

    implementation("com.jakewharton.timber:timber:5.0.1")
}

// Hilt: Allow references to generated code
kapt {
    correctErrorTypes = true
}

detekt {
    buildUponDefaultConfig = true
    config = files("$rootDir/config/detekt/detekt.yml")
}