# Android App Release Setup Guide

This guide walks you through setting up signing and building release versions of the Suled Android app.

## 🔐 Step 1: Generate Release Keystore (One-Time Setup)

### Option A: Using Android Studio (Recommended)
1. Open project in Android Studio
2. Go to **Build → Generate Signed Bundle / APK**
3. Select **Android App Bundle** or **APK**
4. Click **Create new...**
5. Fill in the form:
   - **Key store path**: `D:\Projects\Software\Suled\suled-mobile\android\app\suled-release-key.jks`
   - **Password**: Choose a strong password
   - **Alias**: `suled`
   - **Alias password**: Choose a password (can be same as keystore)
   - **Validity**: 25 years (default)
   - **Certificate info**: Fill in your details

### Option B: Using Command Line
```powershell
# Navigate to the android/app directory
cd D:\Projects\Software\Suled\suled-mobile\android\app

# Generate keystore
keytool -genkey -v -keystore suled-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias suled

# You'll be prompted for:
# - Keystore password
# - Key password
# - Your name, organization, city, state, country
```

**⚠️ IMPORTANT: Backup your keystore file and passwords!**
- Store `suled-release-key.jks` in a safe location
- Save passwords in a password manager
- If you lose this, you can NEVER update your app on Google Play

## 📝 Step 2: Configure Environment Variables

Create a file to store your signing credentials securely.

### Create `keystore.properties` (NOT committed to git)

Create this file: `D:\Projects\Software\Suled\suled-mobile\android\app\keystore.properties`

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=suled
storeFile=suled-release-key.jks
```

**This file is already in .gitignore - never commit it!**

## 🔨 Step 3: Build Release Version

Once configured, use these commands:

### Build Release APK (Direct Distribution)
```powershell
cd D:\Projects\Software\Suled\suled-mobile\android\app
.\gradlew.bat assembleRelease

# Output: app\build\outputs\apk\release\app-release.apk
```

### Build Release AAB (Google Play Store)
```powershell
.\gradlew.bat bundleRelease

# Output: app\build\outputs\bundle\release\app-release.aab
```

### Build Debug APK (Testing)
```powershell
.\gradlew.bat assembleDebug

# Output: app\build\outputs\apk\debug\app-debug.apk
```

## 📱 Step 4: Install APK on Device

### Via ADB (USB)
```powershell
# Enable USB debugging on your Android device first
adb install app\build\outputs\apk\release\app-release.apk
```

### Via File Sharing
1. Copy APK to device via USB, email, or cloud storage
2. On device: Enable **Settings → Security → Install from Unknown Sources**
3. Tap the APK file to install

## 🚀 Step 5: Publishing to Google Play Store

### Prerequisites
- Google Play Developer account ($25 one-time fee)
- Signed AAB file
- App assets (icon, screenshots, description)
- Privacy policy (if using permissions)

### Steps
1. Go to [Google Play Console](https://play.google.com/console)
2. Create new app
3. Fill in app details
4. Upload AAB: `app\build\outputs\bundle\release\app-release.aab`
5. Complete store listing
6. Submit for review

## 🔍 Verification

### Check APK is Signed
```powershell
# Verify the APK signature
jarsigner -verify -verbose -certs app\build\outputs\apk\release\app-release.apk
```

### Get APK Info
```powershell
# View APK details
aapt dump badging app\build\outputs\apk\release\app-release.apk
```

## 📦 Version Management

Update version before each release in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2  // Increment for each release
    versionName = "1.1.0"  // User-facing version
}
```

**Version Code Rules:**
- Must increment for each Play Store upload
- Can never decrease
- Users won't see this number

**Version Name:**
- User-facing (e.g., "1.0", "1.1.0", "2.0.0-beta")
- Use semantic versioning: MAJOR.MINOR.PATCH

## 🛡️ Security Best Practices

1. ✅ **Never commit keystore files or passwords to git**
2. ✅ **Backup keystore in multiple secure locations**
3. ✅ **Use strong passwords (16+ characters)**
4. ✅ **Enable ProGuard/R8 for release builds** (code obfuscation)
5. ✅ **Test release builds before publishing**

## 🐛 Troubleshooting

### "Failed to read key from keystore"
- Check passwords in `keystore.properties`
- Verify keystore file path is correct
- Ensure keystore file exists

### "SigningConfig not found"
- Ensure `keystore.properties` file exists
- Check file path in error message

### "APK won't install"
- Uninstall debug version first (different signature)
- Enable "Install from Unknown Sources"
- Check device compatibility (minSdk = 26, Android 8.0+)

## 📚 Resources

- [Android App Signing Documentation](https://developer.android.com/studio/publish/app-signing)
- [Publish to Google Play](https://developer.android.com/studio/publish)
- [App Bundle vs APK](https://developer.android.com/guide/app-bundle)

---

## Current Configuration

- **Package**: `com.suled.app`
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Version**: 1.0 (code: 1)
- **Build Type**: Debug uses local backend, Release uses Azure backend
