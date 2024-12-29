plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id ("kotlin-parcelize")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.soundplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.soundplayer"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    val lottieVersion = "6.1.0"
    val lifecycle_version = "2.6.2"
    val roomVersion = "2.5.0"
    val nav_version = "2.5.3"

    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")



    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")


    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")

    implementation ("androidx.fragment:fragment-ktx:1.6.2")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    //datatore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    //Lottie  https://github.com/airbnb/lottie-android
    implementation ("com.airbnb.android:lottie:$lottieVersion")

    implementation( "androidx.media3:media3-exoplayer:1.4.1")
    implementation( "androidx.media3:media3-exoplayer-dash:1.4.1")
    implementation ("androidx.media3:media3-ui:1.4.1")
    implementation ("androidx.media3:media3-session:1.4.1")

    // https://github.com/tankery/CircularSeekBar
    implementation ("me.tankery.lib:circularSeekBar:1.4.2")

    //splshScreen
    implementation("androidx.core:core-splashscreen:1.0.1")


   //GLide
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    androidTestImplementation ("androidx.room:room-testing:2.6.1")
    androidTestImplementation ("com.google.truth:truth:1.2.0")

    testImplementation ("com.google.truth:truth:1.2.0")
    //Kotlin-coroutineTeste
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    //Dependência da Biblioteca Mockito
    testImplementation ("org.mockito:mockito-core:5.14.2")

}
kapt {
    correctErrorTypes = true
}