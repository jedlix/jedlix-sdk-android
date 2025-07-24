import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.compiler)
    kotlin("android")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

android {
    compileSdk = 36
    namespace="com.jedlix.sdk"

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFile("proguard-rules.pro")
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    dependencies {
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.app.compat)
        implementation(libs.material)

        implementation(libs.kotlinx.serialization.json)

        implementation(libs.ktor.core)
        implementation(libs.ktor.android)
        implementation(libs.ktor.content.negotiation)
        implementation(libs.ktor.serialization)
        implementation(libs.ktor.logging)

        implementation(libs.androidx.activity)
        implementation(libs.androidx.lifecycle.runtime)
        implementation(libs.androidx.lifecycle.viewmodel)
        implementation(libs.androidx.lifecycle.livedata)
        implementation(libs.androidx.browser)

        /* Compose */
        implementation(libs.compose.foundation)
        implementation(libs.compose.ui)
        implementation(libs.compose.ui.tooling)
        implementation(libs.compose.activity)

        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.test.junit)
        androidTestImplementation(libs.androidx.test.espresso)
    }
}

signing {
    setRequired(
        {
            gradle.taskGraph.hasTask("publish") && gradle.taskGraph.allTasks.any { it.name.contains("Sonatype") }
        }
    )

    sign(publishing.publications)
}

val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)
val javadocJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

afterEvaluate {
    publishing {
        publications {
            val sdkVersion: String by project
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.jedlix"
                artifactId = "sdk"
                version = sdkVersion
            }
            create<MavenPublication>("debug") {
                from(components["debug"])

                groupId = "com.jedlix"
                artifactId = "sdk-debug"
                version = sdkVersion
            }
        }

        publications.withType<MavenPublication> {

            // Stub javadoc.jar artifact
            artifact(javadocJar.get())

            // Provide artifacts information required by Maven Central
            pom {
                name.set(project.name)
                description.set("SDK for connecting to the Jedlix API")
                url.set("https://github.com/jedlix/jedlix-sdk-android")

                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("jedlix")
                        name.set("Jedlix BV")
                    }
                }
                scm {
                    url.set("https://github.com/jedlix/jedlix-sdk-android")
                }
            }
        }
    }

    tasks.withType(AbstractPublishToMaven::class.java).configureEach {
        dependsOn(tasks.withType(Sign::class.java))
    }
}
