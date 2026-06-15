# Sorteio Master ProGuard Rules
-keep class com.sorteiomaster.data.model.** { *; }
-keep class com.sorteiomaster.domain.** { *; }
-keepattributes *Annotation*
-keepattributes Signature

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Jsoup
-keep class org.jsoup.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
