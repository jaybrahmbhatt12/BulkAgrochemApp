# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Retrofit 2 rules
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowoptimization interface * {
    @retrofit2.http.* <methods>;
}

# Retain the Renaissance/Retrofit types.
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep the API interface
-keep interface com.bulkagrochemapp.network.ApiService { *; }

# Keep GSON data classes - Corrected package name (was models, should be model)
-keep class com.bulkagrochemapp.model.** { *; }
-keepclassmembers class com.bulkagrochemapp.model.** { *; }

# OkHttp3 rules
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Gson rules
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# Razorpay
-keep class com.razorpay.** {*;}
-dontwarn com.razorpay.**

# Standard 16KB page size compatibility often requires ensuring native libraries are handled correctly,
# which is done in build.gradle, but we also ensure no weird stripping of native bridge code.
-keepclasseswithmembernames class * {
    native <methods>;
}
