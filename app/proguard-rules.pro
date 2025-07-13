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

# Compose specific rules
-keep class androidx.compose.** { *; }
-keep class androidx.activity.ComponentActivity { *; }

# Prevent R8 from removing ViewModels and Hilt classes
-keep class com.bhavesh.unsplashapp.** { *; }

# For Hilt
-keep class dagger.hilt.** { *; }
-dontwarn dagger.hilt.**

# Coil (Image loading)
-keep class coil.** { *; }
-dontwarn coil.**

# Retrofit + Gson/Moshi
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.moshi.** { *; }
-keep class com.google.gson.** { *; }
-dontwarn com.squareup.moshi.**

# Retrofit API interfaces
-keep interface com.bhavesh.unsplashapp.data.api.** { *; }