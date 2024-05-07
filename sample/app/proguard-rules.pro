# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#####################################sdk-sensorgame###########################
-keep class com.tenmeter.smlibrary.** { *; }
-keep class fi.iki.elonen.** { *; }

#####################################sdk-aliagent###########################
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

-keepattributes Signature

