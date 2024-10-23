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

##---------------Begin: Netty  ----------
-dontwarn io.netty.**
-dontwarn sun.**
-keepattributes Signature,InnerClasses
-keepclasseswithmembers class io.netty.** {
    *;
}
##---------------End: Netty  ----------

##---------------Begin: Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-dontwarn sun.misc.**
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { <fields>; }

# Prevent proguard from stripping interface information from TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# keep yourself
#-keep class com.xxx.xxx.xxx { *; }
##---------------End: Gson  ----------

-keep class com.st.letter.lib.media.**{*;}
-keep class com.st.letter.lib.bean.**{*;}

#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}

-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

#okio
-dontwarn okio.**
-keep class okio.**{*;}

-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

#glide
#如果你的 target API 低于 Android API 27，请添加：
#-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
#如果你使用 DexGuard 你可能还需要添加：
#for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}


-dontwarn com.arialyy.aria.**
-keep class com.arialyy.aria.**{*;}
-keep class **$$DownloadListenerProxy{ *; }
-keep class **$$UploadListenerProxy{ *; }
-keep class **$$DownloadGroupListenerProxy{ *; }
-keepclasseswithmembernames class * {
    @Download.* <methods>;
    @Upload.* <methods>;
    @DownloadGroup.* <methods>;
}


-printmapping mapping.txt
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-ignorewarnings