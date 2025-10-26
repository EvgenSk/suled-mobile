# IntelliJ IDEA Setup for Android Development

This guide covers setting up IntelliJ IDEA Ultimate for Android development with Kotlin and Jetpack Compose.

## Prerequisites

### 1. Install IntelliJ IDEA Ultimate

IntelliJ IDEA Community Edition **does not support** Android development. You need **Ultimate Edition**.

```powershell
# Install IntelliJ IDEA Ultimate
winget install JetBrains.IntelliJIDEA.Ultimate
```

**Note**: Android Studio is built on IntelliJ IDEA and includes all Android tools by default. If you don't have IntelliJ IDEA Ultimate, you can use Android Studio instead:

```powershell
winget install Google.AndroidStudio
```

### 2. Install Required Components

#### Java Development Kit (JDK)
```powershell
# Install JDK 17 (required for Android development)
winget install Microsoft.OpenJDK.17
```

#### Android SDK (If using IntelliJ IDEA)
If you're using IntelliJ IDEA Ultimate (not Android Studio), you need to install Android SDK separately:

1. Download Android Command Line Tools from:
   https://developer.android.com/studio#command-tools

2. Extract to a folder, e.g., `C:\Android\cmdline-tools\latest`

3. Set environment variables:
   ```powershell
   # Add to system environment variables
   [System.Environment]::SetEnvironmentVariable('ANDROID_HOME', 'C:\Android', [System.EnvironmentVariableTarget]::User)
   [System.Environment]::SetEnvironmentVariable('ANDROID_SDK_ROOT', 'C:\Android', [System.EnvironmentVariableTarget]::User)
   ```

4. Install SDK components:
   ```powershell
   cd C:\Android\cmdline-tools\latest\bin
   
   # Accept licenses
   .\sdkmanager --licenses
   
   # Install required SDK components
   .\sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   .\sdkmanager "system-images;android-34;google_apis;x86_64"
   ```

## IntelliJ IDEA Configuration

### 1. Install Android Plugin

1. Open IntelliJ IDEA
2. Go to **File → Settings → Plugins**
3. Search for "Android"
4. Install **Android** plugin by JetBrains
5. Restart IntelliJ IDEA

### 2. Configure Android SDK

1. Go to **File → Project Structure → SDKs**
2. Click **+** → **Android SDK**
3. Browse to your Android SDK location (e.g., `C:\Android`)
4. Click **OK**

### 3. Configure JDK

1. Go to **File → Project Structure → SDKs**
2. Verify JDK 17 is listed
3. If not, click **+** → **JDK** and browse to JDK installation

## Open Suled Project

### 1. Open Project

1. Open IntelliJ IDEA
2. Select **File → Open**
3. Navigate to `mobile/SuledApp`
4. Click **OK**
5. Wait for Gradle sync to complete

### 2. Project Structure Should Look Like:

```
SuledApp/
├── .idea/                    # IntelliJ project settings
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/tournament/app/
│   │       └── res/
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

### 3. Gradle Sync

After opening, IntelliJ will automatically:
- Download Gradle wrapper
- Download project dependencies
- Index the project

**If sync fails**, try:
```powershell
# In terminal within IntelliJ (Alt+F12)
.\gradlew clean
.\gradlew build
```

## Running the App

### 1. Create Android Virtual Device (AVD)

1. In IntelliJ, go to **Tools → Device Manager**
2. Click **+** to create a new virtual device
3. Select **Pixel 5** or any modern device
4. Select **Android API 34** system image
5. Click **Finish**

### 2. Run Configuration

1. Click **Add Configuration** in the top toolbar
2. Click **+** → **Android App**
3. Name it "Suled"
4. Module: select `SuledApp.app.main`
5. Click **OK**

### 3. Run the App

**Option 1: Using Emulator**
1. Select your run configuration and emulator
2. Click the green **Run** button (Shift+F10)

**Option 2: Using Physical Device**
1. Enable Developer Options on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"
2. Connect device via USB
3. Select your device in the run configuration
4. Click **Run**

## IntelliJ IDEA vs Android Studio

### IntelliJ IDEA Ultimate Advantages
✅ Better overall IDE performance
✅ More powerful for full-stack development
✅ Better Kotlin support (JetBrains makes both)
✅ Superior code analysis and refactoring
✅ Database tools included
✅ Better for multi-language projects

### Android Studio Advantages
✅ Pre-configured for Android development
✅ Includes Android SDK by default
✅ Layout Inspector and Profiler tools
✅ Free and open source
✅ Official Google support
✅ Better device management

### Recommendation
- **Use Android Studio** if you only develop Android apps
- **Use IntelliJ IDEA Ultimate** if you develop full-stack (backend + mobile)

Since this project has both Azure Functions backend and Android frontend, **IntelliJ IDEA Ultimate is a good choice** if you have a license.

## Useful IntelliJ IDEA Shortcuts

| Action | Shortcut |
|--------|----------|
| Run | Shift+F10 |
| Build | Ctrl+F9 |
| Search Everywhere | Double Shift |
| Go to Class | Ctrl+N |
| Go to File | Ctrl+Shift+N |
| Recent Files | Ctrl+E |
| Terminal | Alt+F12 |
| Refactor This | Ctrl+Alt+Shift+T |
| Code Completion | Ctrl+Space |
| Parameter Info | Ctrl+P |

## Troubleshooting

### Gradle Sync Issues

1. **Invalidate Caches**:
   - **File → Invalidate Caches / Restart**
   - Select "Invalidate and Restart"

2. **Clear Gradle Cache**:
   ```powershell
   # Delete .gradle folder in project root
   Remove-Item -Recurse -Force .gradle
   
   # In IntelliJ terminal
   .\gradlew clean
   ```

3. **Update Gradle Wrapper**:
   ```powershell
   .\gradlew wrapper --gradle-version 8.2
   ```

### SDK Issues

1. **SDK Not Found**:
   - **File → Project Structure → SDKs**
   - Add Android SDK manually

2. **Build Tools Missing**:
   ```powershell
   # In Android SDK location
   cd C:\Android\cmdline-tools\latest\bin
   .\sdkmanager "build-tools;34.0.0"
   ```

### Emulator Issues

1. **Emulator Won't Start**:
   - Enable Virtualization in BIOS
   - Install Intel HAXM or AMD Hypervisor

2. **Slow Emulator**:
   - Allocate more RAM in AVD settings
   - Use x86_64 system image (not ARM)

### Kotlin Not Recognized

1. **Install Kotlin Plugin**:
   - **File → Settings → Plugins**
   - Search for "Kotlin"
   - Install if not present

2. **Update Kotlin Version**:
   - Check `build.gradle.kts` for Kotlin version
   - Should be 1.9.20 or higher

## Additional Resources

- **IntelliJ IDEA Android Docs**: https://www.jetbrains.com/help/idea/android.html
- **Kotlin Documentation**: https://kotlinlang.org/docs/home.html
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Android Developer Guide**: https://developer.android.com/

## Next Steps

1. ✅ IntelliJ IDEA Ultimate installed
2. ✅ Android plugin enabled
3. ✅ Android SDK configured
4. ✅ Project opened in IntelliJ
5. ✅ AVD created
6. 🚀 Ready to run the app!

Now you can start developing the Suled Android app in IntelliJ IDEA!
