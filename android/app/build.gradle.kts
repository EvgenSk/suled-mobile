plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

// Custom task for IntelliJ: assemble only debug to avoid release signing issues
tasks.register("assembleDebugOnly") {
    group = "build"
    description = "Assembles only the debug variant (useful when release signing is not configured)"
    dependsOn(":app:assembleDebug")
}

