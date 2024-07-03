
How to use local aars?

```kotlin
dependencies {
    //Required
    implementation("libs/sdk-base-v{latest_version}.aar")
    implementation("libs/sdk-fitcloud-v{latest_version}.aar")
    implementation("libs/sdk-realtek-dfu-v{latest_version}.aar")//Use for dfu
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.22"))
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.annotation:annotation:1.5.0")
    implementation("io.reactivex.rxjava3:rxjava:3.1.5")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation("com.polidea.rxandroidble3:rxandroidble:1.17.2")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("androidx.palette:palette-ktx:1.0.0")//Use for create watchface bitmap

    //Optional. Sensor game function
    implementation("libs/sdk-sensorgame-v{latest_version}.aar")

    //Optional. AliAgent function
    implementation("libs/sdk-aliagent-v{latest_version}.aar")

    //Optional. Dfu function for Nordic chip
    implementation("no.nordicsemi.android:dfu:2.2.2")

    //Optional. Built-in music control function
    implementation(libs.androidx.media)

    //Optional. LogoWriter function
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.25")

    //Optional. Some APIs compatible with 1.x.x are provided to minimize changes during migration.
    //Only use for migration, and you need to replace it with new APIs as soon as possible and than remove this dependency.
    implementation("libs/sdk-fitcloud-v1.x.x-compat.aar")
}
```

# sdk-aliagent-v{latest_version}.aar
When you use this aar, you also need to add some additional dependencies and proguard rules.

```kotlin
dependencies {
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.8.1")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.4")
    implementation("com.alibaba:fastjson:1.2.83")
    implementation("org.apache.commons:commons-text:1.9")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("com.aliyun.dpa:oss-android-sdk:2.9.13")
}
```

```kotlin
-keepattributes Signature
        
-keep class com.alibaba.aliagentsdk.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.fd.aliiot.core.** { *; }
-keep class org.eclipse.paho.**{*;}
-keep class com.alibaba.sdk.android.oss.** { *; }
-keep class com.alibaba.fastjson.**{*; }
-keep class org.json.** { *; }

-dontwarn okio.**
-dontwarn com.google.gson.**
-dontwarn com.alibaba.fastjson.**
-dontwarn org.apache.commons.codec.binary.**
```
