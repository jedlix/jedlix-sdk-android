// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        val androidGradlePluginVersion: String by project
        val kotlinVersion: String by project
        classpath("com.android.tools.build:gradle:$androidGradlePluginVersion")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$kotlinVersion")
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
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set("0.43.2")
        enableExperimentalRules.set(false)
        ignoreFailures.set(false)
        disabledRules.set(setOf("no-blank-line-before-rbrace", "no-wildcard-imports"))
        verbose.set(true)
        filter {
            exclude { it.file.path.contains("build/") }
        }
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
