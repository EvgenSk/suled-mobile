# Firebase Crashlytics Setup

This app uses Firebase Crashlytics for crash reporting and error monitoring in production builds.

## Setup Instructions

### 1. Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or select an existing project
3. Follow the setup wizard

### 2. Register Your Android App

1. In the Firebase console, go to Project Settings
2. Click "Add app" and select Android
3. Enter your package name: `com.suled.app`
4. Download the `google-services.json` file
5. Place it in `android/app/app/` directory (same level as `build.gradle.kts`)

**Important:** The `google-services.json` file contains your Firebase configuration and should **NOT** be committed to version control. It's already added to `.gitignore`.

### 3. Enable Crashlytics

1. In Firebase Console, go to Crashlytics
2. Enable Crashlytics for your app
3. Follow any additional setup steps

### 4. Test Crashlytics

To test that Crashlytics is working:

```kotlin
// Force a test crash in debug build
Button(onClick = { throw RuntimeException("Test Crash") }) {
    Text("Test Crash")
}
```

After forcing a crash and restarting the app, check the Firebase Console Crashlytics dashboard (crashes may take a few minutes to appear).

## How It Works

- **Debug builds**: Timber logs to Logcat using `DebugTree`
- **Release builds**: Timber logs warnings and errors to Firebase Crashlytics using `CrashlyticsTree`
- All exceptions logged with `Timber.e(exception, message)` are automatically sent to Crashlytics

## ProGuard/R8

Crashlytics mapping files are automatically uploaded during release builds thanks to the `com.google.firebase.crashlytics` Gradle plugin. This ensures stack traces are deobfuscated in the Firebase console.

## Dependencies

The following Firebase dependencies are included:

```groovy
implementation(platform("com.google.firebase:firebase-bom:33.11.0"))
implementation("com.google.firebase:firebase-crashlytics-ktx")
implementation("com.google.firebase:firebase-analytics-ktx")
```

## Development Without Firebase

If you want to build the app without Firebase:

1. Comment out the Firebase plugins in `app/build.gradle.kts`:
   ```kotlin
   // id("com.google.gms.google-services")
   // id("com.google.firebase.crashlytics")
   ```

2. Comment out the Firebase dependencies in `app/build.gradle.kts`

3. Update `SuledApplication.kt` to use a no-op tree for release builds

However, it's recommended to set up a Firebase project (free tier) to enable crash reporting in production.
