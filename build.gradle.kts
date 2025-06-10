// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.kotlinter.plugin)
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

val sonatypeCentralUserToken: String? by project
val sonatypeCentralUserTokenPassword: String? by project


// Stub secrets to let the project sync and build without the publication values set up
ext["signing.keyId"] = signingKeyId ?: System.getenv("SIGNING_KEY_ID")
ext["signing.password"] = signingPassword ?: System.getenv("SIGNING_PASSWORD")
ext["signing.secretKeyRingFile"] = signingSecretKeyRingFile ?: System.getenv("SIGNING_SECRET_KEY_RING_FILE")

group = "com.jedlix"

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(sonatypeCentralUserToken)
            password.set(sonatypeCentralUserTokenPassword)
        }
    }
}

allprojects {
    apply(plugin = "org.jmailen.kotlinter")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
