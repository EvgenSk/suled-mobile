# Setting Up Java and Android Studio for Testing

Since Java and Android Studio are not currently installed, here's how to set them up:

## Option 1: Install Android Studio (Recommended - Includes Everything)

Android Studio includes everything you need: Java JDK, Android SDK, and Gradle.

### Download and Install

1. **Download Android Studio**
   - Go to: https://developer.android.com/studio
   - Click "Download Android Studio"
   - Accept terms and download

2. **Install Android Studio**
   - Run the installer
   - Choose "Standard" installation
   - Accept all default settings
   - Wait for it to download SDK components (~2-3 GB)

3. **Open Your Project**
   - Launch Android Studio
   - Select "Open" from welcome screen
   - Navigate to: `D:\Projects\Software\Suled\suled-mobile\android\app`
   - Click OK

4. **Wait for Gradle Sync**
   - Android Studio will automatically sync Gradle
   - This downloads all dependencies (may take 5-10 minutes first time)
   - Wait for "Gradle build finished" message

5. **Run Tests**
   - In Project view, right-click on `app/src/test`
   - Select "Run 'All Tests'"
   - Watch 48 tests execute! ✅

### After Installation

Android Studio's terminal will have Java configured automatically:
```powershell
# In Android Studio Terminal tab:
./gradlew test
```

## Option 2: Install Java JDK Only (Lightweight)

If you just want to run tests from command line without Android Studio:

### Download JDK 17

1. **Eclipse Temurin (Recommended - Free, Open Source)**
   - Go to: https://adoptium.net/
   - Download: OpenJDK 17 (LTS)
   - Choose: Windows x64 installer (.msi)
   - Install with default options (will set JAVA_HOME automatically)

2. **Or Oracle JDK**
   - Go to: https://www.oracle.com/java/technologies/downloads/#java17
   - Download: Windows x64 Installer
   - Install and accept license

### Verify Installation

Open a NEW PowerShell window and run:
```powershell
java -version
```

Expected output:
```
openjdk version "17.0.x" 2024-xx-xx
```

### Set Environment Variables (if not automatic)

If `java -version` doesn't work:

```powershell
# Run PowerShell as Administrator
$javaPath = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"  # Adjust version

# Set JAVA_HOME
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, "Machine")

# Add to PATH
$path = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
[System.Environment]::SetEnvironmentVariable("Path", "$path;$javaPath\bin", "Machine")
```

Restart your terminal and test: `java -version`

### Download Gradle (if Java only)

Since the Gradle wrapper JAR is missing:

1. Go to: https://gradle.org/releases/
2. Download: Gradle 8.0+ (Binary-only)
3. Extract to: `C:\Gradle`
4. Add to PATH: `C:\Gradle\gradle-8.x\bin`

Or let Android Studio handle this.

## Option 3: Use Gradle from Existing Installation

If you have IntelliJ IDEA or other JetBrains IDE:

1. Find their bundled JDK:
   ```powershell
   Get-ChildItem "C:\Program Files\JetBrains" -Recurse -Directory -Filter "jbr" | Select-Object FullName
   ```

2. Set JAVA_HOME to that location

## Comparison: Which Option?

| Option | Pros | Cons | Best For |
|--------|------|------|----------|
| **Android Studio** | ✅ Everything included<br>✅ Best IDE for Android<br>✅ Easy test running<br>✅ Visual test results | ❌ Large download (~1GB)<br>❌ Takes time to install | **Android development** |
| **JDK Only** | ✅ Lightweight (~200MB)<br>✅ Command-line friendly | ❌ Need to install Gradle<br>❌ No visual IDE<br>❌ More manual setup | **CI/CD or minimal setup** |
| **Existing JDK** | ✅ No download<br>✅ Instant | ❌ May not have JDK 17<br>❌ Still need Gradle | **Quick testing** |

## Recommended Path: Android Studio

For your situation, I recommend **Android Studio** because:
- ✅ You're developing an Android app
- ✅ You'll need it for debugging/testing on devices
- ✅ It handles all configuration automatically
- ✅ Best experience for running and viewing test results
- ✅ Includes everything: Java, Android SDK, Gradle, Emulator

## After Setup: Running Tests

Once you have either option installed:

### In Android Studio (Easiest)
1. Right-click `app/src/test` folder
2. Click "Run 'All Tests'"
3. See results in Run panel

### In Command Line
```powershell
cd D:\Projects\Software\Suled\suled-mobile\android\app

# Run all unit tests
.\gradlew.bat test

# View results
start app\build\reports\tests\test\index.html
```

## What You'll Get

After running tests, you'll see:
```
BUILD SUCCESSFUL in 15s

48 tests completed, 48 passed ✅

Test Results:
- PairSelectionViewModelTest: 10/10 passed
- GamesViewModelTest: 10/10 passed
- TournamentRepositoryTest: 19/19 passed
- PairSelectionScreenTest: 9/9 passed
```

## Time Estimates

- **Android Studio Install**: ~30 minutes (download + install + first sync)
- **JDK Only Install**: ~10 minutes (download + install + setup)
- **First Test Run**: ~2 minutes (Gradle downloads dependencies)
- **Subsequent Test Runs**: ~10 seconds

## Need Help?

If you run into issues:
1. Make sure you're using a NEW PowerShell window after installation
2. Restart VS Code after installation
3. Check that JAVA_HOME is set: `$env:JAVA_HOME`
4. Verify Java works: `java -version`

## Quick Decision

**Want to start quickly?** → Install Android Studio (it handles everything)

**Already familiar with command-line tools?** → Install JDK 17 only

**Want to see tests run NOW?** → Install Android Studio, open project, run tests

---

**Next Step**: Install Android Studio from https://developer.android.com/studio
