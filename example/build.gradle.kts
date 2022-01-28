plugins {
    id("com.android.application")
    kotlin("android")
    id("com.github.triplet.play") version "3.7.0"
}

android {
    compileSdk = 31
    val buildCode: String? by project

    defaultConfig {
        applicationId = "com.jedlix.sdk.example"
        minSdk = 21
        targetSdk = 31
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
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        dataBinding = true
    }

    dataBinding {
        addKtx = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

play {
    val serviceAccountLocation: String? by project
    serviceAccountCredentials.set(file(serviceAccountLocation ?: "default"))
    track.set("internal")
    defaultToAppBundles.set(true)
}

dependencies {
    val androidCoreVersion: String by project
    val appCompatVersion: String by project
    val materialVersion: String by project
    val constraintLayoutVersion: String by project
    val androidxActivityVersion: String by project
    val androidxLifecycleVersion: String by project

    implementation("androidx.core:core-ktx:$androidCoreVersion")
    implementation("androidx.appcompat:appcompat:$appCompatVersion")
    implementation("com.google.android.material:material:$materialVersion")
    implementation("androidx.constraintlayout:constraintlayout:$constraintLayoutVersion")

    implementation(project(":sdk"))

    implementation("com.auth0.android:auth0:2.6.0")
    implementation("com.auth0.android:jwtdecode:2.0.1")

    implementation("androidx.activity:activity-ktx:$androidxActivityVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$androidxLifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycleVersion")

    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}
