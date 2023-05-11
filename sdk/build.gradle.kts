plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("maven-publish")
    id("signing")
}

android {
    compileSdk = 33
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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
        implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
        implementation("io.ktor:ktor-client-logging:$ktorVersion")

        implementation("androidx.activity:activity-ktx:$androidxActivityVersion")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:$androidxLifecycleVersion")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$androidxLifecycleVersion")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:$androidxLifecycleVersion")
        implementation("androidx.browser:browser:$androidxBrowserVersion")

        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
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
