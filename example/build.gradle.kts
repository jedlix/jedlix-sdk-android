plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.github.triplet.play") version "3.7.0"
    alias(libs.plugins.compose.compiler)
}

android {
    compileSdk = 35
    namespace="com.jedlix.sdk.example"
    val buildCode: String? by project

    defaultConfig {
        targetSdk = 34
        applicationId = "com.jedlix.sdk.example"
        minSdk = 21
        versionCode = buildCode?.toInt() ?: 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders.putAll(mapOf("auth0Domain" to "", "auth0Scheme" to "https"))
    }

    signingConfigs {
        create("release") {
            val keystorePath: String? by project
            val keystorePassword: String? by project
            storeFile = file(keystorePath ?: "default")
            storePassword = keystorePassword ?: "default"
            keyAlias = "upload-key"
            keyPassword = keystorePassword ?: "default"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.compiler.extension.get().toString()
    }

    buildFeatures {
        dataBinding = true
    }

    dataBinding {
        enable = true
        addKtx = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

play {
    val serviceAccountLocation: String? by project
    serviceAccountCredentials.set(file(serviceAccountLocation ?: "default"))
    track.set("internal")
    defaultToAppBundles.set(true)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.app.compat)
    implementation(libs.material)
    implementation(libs.androix.constraint.layout)

    val exampleAsMavenLocal: String by project
    if (exampleAsMavenLocal.toBoolean()) {
        implementation(libs.jedlix.sdk)
    } else {
        implementation(project(":sdk"))
    }

    implementation(libs.auth0)
    implementation(libs.auth0.jwtdecode)

    implementation(libs.androidx.activity)
    implementation(libs.compose.activity)

    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.espresso)
}
