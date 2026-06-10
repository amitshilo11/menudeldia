import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

val versionProps = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.metro)
    alias(libs.plugins.googleServices)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MenudizApp"
            isStatic = true
        }
    }

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val iosMain by creating {
            dependsOn(commonMain.get())
        }
        iosArm64Main.get().dependsOn(iosMain)
        iosSimulatorArm64Main.get().dependsOn(iosMain)

        val webMain by creating {
            dependsOn(commonMain.get())
        }
        jsMain.get().dependsOn(webMain)
        wasmJsMain.get().dependsOn(webMain)

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.preview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.navigation.compose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.metro.runtime)
            implementation(libs.kotlinx.datetime)
            implementation(projects.shared)
        }

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.maps.compose)
            implementation(libs.play.services.location)
            implementation(libs.timber)
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.amitshilo.menudeldia"
    compileSdk = versionProps["androidCompileSdk"].toString().toInt()

    defaultConfig {
        applicationId = "com.amitshilo.menudeldia"
        minSdk = versionProps["androidMinSdk"].toString().toInt()
        targetSdk = versionProps["androidTargetSdk"].toString().toInt()
        versionCode = versionProps["versionCode"].toString().toInt()
        versionName = versionProps["versionName"].toString()
        manifestPlaceholders["MAPS_API_KEY"] = localProps["MAPS_API_KEY"]?.toString() ?: ""
        // Web OAuth Client ID from Google Cloud Console (NOT the Android client ID).
        // Set GOOGLE_WEB_CLIENT_ID=your_client_id in local.properties.
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProps["GOOGLE_WEB_CLIENT_ID"] ?: ""}\""
        )
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        create("mock") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
}

val syncIosVersion by tasks.registering {
    val xcconfig = rootProject.file("iosApp/Configuration/Config.xcconfig")
    val versionCode = versionProps["versionCode"].toString()
    val versionName = versionProps["versionName"].toString()
    doLast {
        val updated = xcconfig.readText()
            .replace(Regex("CURRENT_PROJECT_VERSION=.*"), "CURRENT_PROJECT_VERSION=$versionCode")
            .replace(Regex("MARKETING_VERSION=.*"), "MARKETING_VERSION=$versionName")
        xcconfig.writeText(updated)
    }
}

tasks.matching { it.name.matches(Regex("link.*(Ios|Framework).*")) }.configureEach {
    dependsOn(syncIosVersion)
}
