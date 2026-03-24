# Kotlinx Serialization — keep serializer metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.quizzer.app.**$$serializer { *; }
-keepclassmembers class com.quizzer.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.quizzer.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-dontwarn dagger.hilt.**
