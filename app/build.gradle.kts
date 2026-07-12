plugins {
    id("com.android.application")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    namespace = "com.example.soundplayer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.soundplayer"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        animationsDisabled = true

        unitTests {
            isReturnDefaultValues = true
        }
    }

    packaging {
        resources {
            excludes +=
                setOf(
                    "META-INF/LICENSE.md",
                    "META-INF/LICENSE-notice.md",
                )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    androidTestImplementation(project(":app"))

    val lifecycleVersion = "2.9.2"
    val roomVersion = "2.7.2"
    val navVersion = "2.9.3"
    val composeBomVersion = "2026.06.00"

    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    implementation("androidx.navigation:navigation-compose:$navVersion")

    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-android-compiler:2.60.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:$lifecycleVersion")
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.8.0")
    implementation("androidx.media3:media3-session:1.8.0")

    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("io.coil-kt:coil-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Unit tests - JUnit5 + MockK
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.google.truth:truth:1.4.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // Instrumented tests - JUnit4 required by the Android test runner
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    androidTestImplementation("androidx.room:room-testing:2.7.2")
    androidTestImplementation("com.google.truth:truth:1.4.4")
    androidTestImplementation("io.mockk:mockk-android:1.13.10")
}

ktlint {
    version = "1.3.1"
    android = true
    ignoreFailures = false
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.HTML)
    }
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

hilt {
    enableAggregatingTask = true
}

val androidTestDevice = providers.gradleProperty("android.test.device").orNull

fun adbCommand(vararg arguments: String): List<String> =
    if (androidTestDevice.isNullOrBlank()) {
        listOf("adb", *arguments)
    } else {
        listOf("adb", "-s", androidTestDevice, *arguments)
    }

tasks.register<Exec>("pushDebugApkForAdbInstrumentedTest") {
    group = "verification"
    description = "Pushes the debug APK to the connected device for direct instrumentation."
    dependsOn("assembleDebug")
    commandLine(
        adbCommand(
            "push",
            layout.buildDirectory
                .file("outputs/apk/debug/app-debug.apk")
                .get()
                .asFile
                .absolutePath,
            "/data/local/tmp/soundplayer-debug.apk",
        ),
    )
}

tasks.register<Exec>("installDebugApkForAdbInstrumentedTest") {
    group = "verification"
    description = "Installs the debug APK using pm install, avoiding adb install hangs on older devices."
    dependsOn("pushDebugApkForAdbInstrumentedTest")
    commandLine(
        adbCommand("shell", "pm", "install", "-r", "-t", "/data/local/tmp/soundplayer-debug.apk"),
    )
}

tasks.register<Exec>("pushDebugAndroidTestApkForAdbInstrumentedTest") {
    group = "verification"
    description = "Pushes the debug androidTest APK to the connected device for direct instrumentation."
    dependsOn("assembleDebugAndroidTest")
    commandLine(
        adbCommand(
            "push",
            layout.buildDirectory
                .file("outputs/apk/androidTest/debug/app-debug-androidTest.apk")
                .get()
                .asFile
                .absolutePath,
            "/data/local/tmp/soundplayer-debug-androidTest.apk",
        ),
    )
}

tasks.register<Exec>("installDebugAndroidTestApkForAdbInstrumentedTest") {
    group = "verification"
    description = "Installs the debug androidTest APK using pm install."
    dependsOn("pushDebugAndroidTestApkForAdbInstrumentedTest")
    commandLine(
        adbCommand("shell", "pm", "install", "-r", "-t", "/data/local/tmp/soundplayer-debug-androidTest.apk"),
    )
}

tasks.register<Exec>("adbDebugAndroidTest") {
    group = "verification"
    description = "Runs the debug instrumentation suite directly through adb am instrument."
    dependsOn(
        "installDebugApkForAdbInstrumentedTest",
        "installDebugAndroidTestApkForAdbInstrumentedTest",
    )
    commandLine(
        adbCommand(
            "shell",
            "am",
            "instrument",
            "-w",
            "com.example.soundplayer.test/androidx.test.runner.AndroidJUnitRunner",
        ),
    )
}
