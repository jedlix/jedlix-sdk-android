plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 21
        targetSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    dependencies {
        val androidCoreVersion: String by project
        val appCompatVersion: String by project
        val materialVersion: String by project
        val serializationVersion: String by project
        val ktorVersion: String by project
        val androidxActivityVersion: String by project
        val androidxLifecycleVersion: String by project
        val androidxBrowserVersion: String by project

        implementation("androidx.core:core-ktx:$androidCoreVersion")
        implementation("androidx.appcompat:appcompat:$appCompatVersion")
        implementation("com.google.android.material:material:$materialVersion")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

        implementation("io.ktor:ktor-client-core:$ktorVersion")
        implementation("io.ktor:ktor-client-android:$ktorVersion")
        implementation("io.ktor:ktor-client-serialization:$ktorVersion")
        implementation("io.ktor:ktor-client-logging:$ktorVersion")

        implementation("androidx.activity:activity-ktx:$androidxActivityVersion")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:$androidxLifecycleVersion")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycleVersion")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycleVersion")
        implementation("androidx.browser:browser:$androidxBrowserVersion")

        testImplementation("junit:junit:4.+")
        androidTestImplementation("androidx.test.ext:junit:1.1.3")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
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

val androidSourcesJar: TaskProvider<Jar> by tasks.registering(Jar::class) {
    dependsOn(javadocJar)
    archiveClassifier.set("sources")
    from(android.sourceSets["main"].java.srcDirs)
}

artifacts {
    archives(javadocJar)
    archives(androidSourcesJar)
}

afterEvaluate {
    publishing {
        publications {
            val libraryVersion: String by project
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = "com.jedlix"
                artifactId = "sdk"
                version = libraryVersion
            }
            create<MavenPublication>("debug") {
                from(components["debug"])

                groupId = "com.jedlix"
                artifactId = "sdk-debug"
                version = libraryVersion
            }
        }

        publications.withType<MavenPublication> {

            // Stub javadoc.jar artifact
            artifact(javadocJar.get())
            artifact(androidSourcesJar.get())

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
}
