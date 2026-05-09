plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.google.services)
}
android {
    compileSdk = Config.COMPILE_SDK
    namespace = Config.APP_ID

    defaultConfig {
        applicationId = Config.APP_ID
        minSdk = Config.MIN_SDK
        targetSdk = Config.TARGET_SDK
        versionCode = 20250721
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        aidl = false
        compose = false
        prefab = false
        resValues = false
        shaders = false
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.timber)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlin.reflect)
    implementation(libs.gson)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
