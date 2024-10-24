// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.3.0"
    id("org.jmailen.kotlinter") version "4.3.0"
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.android.gradle)
        classpath(libs.kotlin.gradle)
        classpath(libs.kotlin.serialization)
        classpath(libs.dokka)
    }
}

val signingKeyId: String? by project
val signingPassword: String? by project
val signingSecretKeyRingFile: String? by project
val ossrhUsername: String? by project
val ossrhPassword: String? by project
val sonatypeStagingProfileId: String? by project

// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = signingKeyId ?: System.getenv("SIGNING_KEY_ID")
ext["signing.password"] = signingPassword ?: System.getenv("SIGNING_PASSWORD")
ext["signing.secretKeyRingFile"] = signingSecretKeyRingFile ?: System.getenv("SIGNING_SECRET_KEY_RING_FILE")
ext["ossrhUsername"] = ossrhUsername ?: System.getenv("OSSRH_USERNAME")
ext["sonatypeStagingProfileId"] = sonatypeStagingProfileId ?: System.getenv("SONATYPE_STAGING_PROFILE_ID")

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(ossrhUsername)
            password.set(ossrhPassword)
            stagingProfileId.set(sonatypeStagingProfileId)
        }
    }
}

allprojects {
    apply(plugin = "org.jmailen.kotlinter")

    // Workaround for Kapt not setting the proper JVM target
    // See https://youtrack.jetbrains.com/issue/KT-55947/Unable-to-set-kapt-jvm-target-version
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "11"
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
