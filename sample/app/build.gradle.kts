plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.navigation)
}
kotlin {
    jvmToolchain(8)
}
android {
    namespace = "com.topstep.fitcloud.sample2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.topstep.fitcloud.sample2"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.2.5.3"
        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    configurations.configureEach {
        resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
        resolutionStrategy.cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
    }
}

afterEvaluate {
    tasks.getByName("installDebug").doLast {
        val versionName = android.defaultConfig.versionName
        val oldApkFile = file("${buildDir}/outputs/apk/debug/app-debug.apk")
        val newApkFile = file("${buildDir}/outputs/apk/debug/FitCloud-sample-v${versionName}.apk")
        if (newApkFile.exists()) {
            newApkFile.delete()
        }
        if (newApkFile.exists()) {
            println("File can't delete")
            return@doLast
        }
        if (!oldApkFile.exists()) {
            println("File can't found")
            return@doLast
        }
        oldApkFile.copyTo(newApkFile)
        if (!newApkFile.exists()) {
            println("File can't create")
        }
    }
}

dependencies {
    //Required
    implementation(platform(libs.kotlin.bom))
    implementation(libs.androidx.core)
    implementation(libs.androidx.annotation)
    implementation(libs.timber)
    implementation(libs.rxjava)
    implementation(libs.rxandroid)
    implementation(libs.rxandroidble)
    implementation(libs.okhttp3)
    implementation(libs.androidx.palette)
    if (isDeveloperEnvironment()) {//For developer environment, use remote dependencies
        implementation("com.topstep.wearkit:sdk-base:3.0.1-beta12")
        implementation("com.topstep.wearkit:sdk-fitcloud:3.0.1-beta12")
    } else {//For author environment, use local project
        implementation(project(":sdk-base"))
        implementation(project(":sdk-fitcloud"))
    }
    implementation("com.topstep.wearkit:ext-realtek-dfu:1.0.2")

    //Optional. Sensor game function
    implementation("com.topstep.wearkit:ext-sensorgame:1.0.3")

    //Optional. AliAgent function
    implementation("com.topstep.wearkit:ext-aliagent-ext:1.0.5")

    //Optional. Dfu function for Nordic chip
    implementation("no.nordicsemi.android:dfu:2.2.2")

    //Optional. Built-in music control function
    implementation(libs.androidx.media)

    //Optional. LogoWriter function
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.25")

    //Base
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx3")
    implementation(libs.google.material)
    implementation(libs.androidx.constraint)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.swiperefresh)

    //JetPack-WorkManager
    implementation(libs.androidx.work)

    //JetPack-SplashScreen
    implementation(libs.androidx.splashscreen)

    //JetPack-DataStore
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)

    //JetPack-Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //JetPack-Navigation
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)

    //JetPack-multidex
    implementation(libs.androidx.multidex)

    //JetPack-CameraX
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.extensions)
    implementation(libs.androidx.window)

    //JetPack-Lifecycle
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.lifecycle.service)

    //JetPack-ViewModel
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.savedstate)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.fragment)

    //Moshi
    implementation(libs.moshi)
    ksp(libs.moshi.compiler)

    //permission
    implementation(libs.permissionx)

    //retrofit2
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.moshi)
    implementation(libs.okhttp3.logging)

    //others
    implementation(files("libs/lib-toolkit-v1.0.3.aar"))
    implementation(libs.mars.xlog)
    implementation(libs.kilnn.wheelview)
    implementation("com.github.zhpanvip:bannerviewpager:3.5.12")

    //glide
    implementation(libs.glide)
    ksp(libs.glide.compiler)
    implementation(libs.glide.okhttp3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}

/**
 * Developers and authors may use different dependencies
 */
fun isDeveloperEnvironment(): Boolean {
    return !project.projectDir.path.toString().contains("android-sdk-wearkit")
}