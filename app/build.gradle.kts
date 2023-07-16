@file:Suppress(
    "UnstableApiUsage",
    "KaptUsageInsteadOfKsp", // remove after Hilt supports KSP
)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.cookpad.android.plugin.license-tools") version "1.2.8"
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

fun convertVersionNameToCode(versionName: String): Int {
    val majorVersion = versionName.split('.')[0].toInt()
    val minorVersion = versionName.split('.')[1].toInt()
    val revision = versionName.split('.')[2].toInt()
    return 10000 * majorVersion + 100 * minorVersion + revision
}

val appVersion = "1.0.20"

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "jp.kawagh.kiando"
        minSdk = 26
        targetSdk = 33
        versionName = appVersion
        versionCode = convertVersionNameToCode(appVersion)
        testInstrumentationRunner = "jp.kawagh.kiando.CustomTestRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
            }
        }
        manifestPlaceholders["icon"] = "@mipmap/ic_launcher"
        manifestPlaceholders["roundIcon"] = "@mipmap/ic_launcher_round"
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
            manifestPlaceholders["icon"] = "@mipmap/ic_debug_launcher"
            manifestPlaceholders["roundIcon"] = "@mipmap/ic_debug_launcher_round"
        }
    }
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
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
    implementation("androidx.core:core-ktx:1.10.1")
    val composeVersion: String by project
    implementation("androidx.compose.ui:ui:$composeVersion")
    val material3Version = "1.1.1"
    implementation("androidx.compose.material3:material3:$material3Version")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    androidTestImplementation("androidx.test:core:1.5.0")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeVersion")

    val navVersion = "2.6.0"
    implementation("androidx.navigation:navigation-compose:$navVersion")

    val roomVersion = "2.5.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-testing:$roomVersion")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
    // change system-ui style
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.25.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.44")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.44")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.7.0")

    implementation("com.jakewharton.timber:timber:5.0.1")

    implementation(platform("com.google.firebase:firebase-bom:31.2.3"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // formatter
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.22.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

}

// Hilt: Allow references to generated code
kapt {
    correctErrorTypes = true
}

detekt {
    buildUponDefaultConfig = true
    config = files("$rootDir/config/detekt/override_detekt.yml")
    autoCorrect = true // `./gradlew detekt correct files
}