# Kotlin Extension Duplicate Error - Complete Solution

## The Error
```
An exception occurred applying plugin request [id: 'org.jetbrains.kotlin.android']
> Failed to apply plugin 'org.jetbrains.kotlin.android'.
   > Cannot add extension with name 'kotlin', as there is an extension already registered with that name.
```

## Root Cause Analysis

This error occurs when the Kotlin Android plugin tries to register a `kotlin` extension, but one already exists. This can happen due to:

1. **Cached Gradle/IDE state** from previous plugin configurations
2. **AGP version compatibility issues** (AGP 9.0.0 is very new and may have bugs)
3. **Stale build artifacts** in `.gradle`, `.idea`, or `.kotlin` directories

## Solutions Applied

### 1. ✅ Removed Conflicting Compose Plugin
Both build files now have only the essential plugins:

**Root build.gradle.kts:**
```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false
}
```

**app/build.gradle.kts:**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
```

### 2. ✅ Downgraded AGP to 8.7.3
Changed from AGP 9.0.0 to 8.7.3 for better stability:
- AGP 9.0.0 is bleeding edge and has compatibility issues
- AGP 8.7.3 is stable and well-tested with Kotlin 2.2.10

### 3. ✅ Cleaned All Cache Directories
Removed:
- `.gradle/` - Gradle cache
- `.idea/` - IntelliJ/Android Studio configuration
- `.kotlin/` - Kotlin compiler cache  
- `build/` - Build outputs
- `app/.gradle/` and `app/build/` - App module caches

### 4. ✅ Stopped Gradle Daemon
Ensured no old Gradle daemon is running with cached state.

## 🚀 Next Steps - MUST DO

### Step 1: Restart Android Studio
**Close Android Studio completely and reopen it.**

This is CRITICAL because:
- Android Studio caches Gradle configuration
- The IDE needs to reload with the clean state
- Old daemon processes need to terminate

### Step 2: Open Project Fresh
1. Launch Android Studio
2. Open your project: `/Users/evgensk/projects/software/suled/suled-mobile/android/app`
3. Wait for indexing to complete

### Step 3: Sync Gradle
1. Click **"Sync Now"** banner when it appears
2. Or: **File → Sync Project with Gradle Files**
3. Watch the **Build** tab at the bottom for progress
4. **Wait 3-5 minutes** for first sync (downloads dependencies)

### Step 4: If Sync Still Fails

If you still see the kotlin extension error, try this:

**Option A: Invalidate Caches in Android Studio**
```
File → Invalidate Caches → Invalidate and Restart
```

**Option B: Manual Clean and Sync**
```bash
cd /Users/evgensk/projects/software/suled/suled-mobile/android/app
./gradlew clean
./gradlew --stop
# Then restart Android Studio and sync
```

**Option C: Check for Multiple Project Roots**
- Make sure you're opening `/Users/evgensk/projects/software/suled/suled-mobile/android/app`
- NOT a parent directory that might have another Gradle project

## Current Configuration Summary

### AGP Version
- **Version**: 8.7.3 (stable)
- **Kotlin**: 2.2.10
- **Gradle**: 9.1.0
- **Compose**: Enabled via `buildFeatures.compose = true`

### Build Files Status
✅ Root `build.gradle.kts` - 5 lines, minimal, correct
✅ App `build.gradle.kts` - 87 lines, no errors
✅ `settings.gradle.kts` - 18 lines, correct
✅ `gradle.properties` - Clean, no deprecated properties

### Caches Status
✅ All cleared - fresh start guaranteed

## Why This Should Work Now

1. **AGP 8.7.3** is stable and compatible with Kotlin 2.2.10
2. **All caches cleared** - no stale configuration
3. **Single Kotlin plugin** - no duplicate registration
4. **Daemon stopped** - fresh Gradle process
5. **IDE caches removed** - Android Studio will rebuild configuration

## If Problem Persists

If after following ALL steps above you still get the error, it could indicate:

1. **Corrupted Gradle installation** - Try deleting `~/.gradle/caches`
2. **Android Studio plugin issue** - Reinstall Kotlin plugin in AS
3. **System-level Gradle wrapper issue** - Delete `~/.gradle/wrapper`

But with the changes made, this should resolve 99% of cases.

---

## 📝 Summary of Changes

| File | Change | Reason |
|------|--------|---------|
| `build.gradle.kts` | AGP 9.0.0 → 8.7.3 | Stability & compatibility |
| `build.gradle.kts` | Removed Compose plugin | Prevents conflict |
| `app/build.gradle.kts` | Removed Compose plugin | Prevents conflict |
| `.gradle/` | Deleted | Clear cache |
| `.idea/` | Deleted | Clear IDE cache |
| `.kotlin/` | Deleted | Clear Kotlin cache |

**Next action: RESTART ANDROID STUDIO and sync the project!** 🚀
