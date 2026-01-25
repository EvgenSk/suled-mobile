# Wear OS ProGuard Rules

# Keep Wear OS classes
-keep class androidx.wear.** { *; }
-keep class com.google.android.gms.wearable.** { *; }

# Keep serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.suled.**$$serializer { *; }
-keepclassmembers class com.suled.** {
    *** Companion;
}
-keepclasseswithmembers class com.suled.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data models
-keep class com.suled.models.** { *; }
-keep class com.suled.data.** { *; }

# Keep complications and tiles
-keep class * extends androidx.wear.watchface.complications.datasource.ComplicationDataSourceService { *; }
-keep class * extends androidx.wear.tiles.TileService { *; }
