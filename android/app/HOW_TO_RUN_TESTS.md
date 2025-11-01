# Running Tests - Quick Start Guide

## ⚠️ Recommended: Use Android Studio

Due to JDK/Gradle compatibility issues with command-line execution, **running tests through Android Studio is strongly recommended**. Android Studio handles all Java/Gradle configuration automatically.

## Prerequisites

Before running tests, ensure you have:

1. **Android Studio** installed (required)
2. **JDK 17** (bundled with Android Studio)
3. Project opened in Android Studio at least once

## ✅ Option 1: Run Tests in Android Studio (RECOMMENDED)

### Run All Unit Tests
1. Open Android Studio
2. Open the project at `suled-mobile/android/app`
3. Right-click on `app/src/test` folder in Project view
4. Select **"Run 'All Tests'"**

### Run Specific Test Class
1. Navigate to the test file (e.g., `PairSelectionViewModelTest.kt`)
2. Click the green play button next to the class name
3. Or right-click the file and select **"Run 'PairSelectionViewModelTest'"**

### Run Single Test
1. Click the green play button next to any `@Test` method
2. View results in the Run panel at the bottom

### View Test Results
- ✅ Green checkmark = Test passed
- ❌ Red X = Test failed
- Click on any test to see details and stack traces

## Option 2: Run Tests from Command Line

### Setup Java Environment First

**Find Java Path** (usually in Android Studio):
```powershell
# Common locations:
C:\Program Files\Android\Android Studio\jbr\bin\java.exe
C:\Program Files\Java\jdk-17\bin\java.exe
```

**Set JAVA_HOME** (in PowerShell):
```powershell
# Set for current session
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Or permanently (run as admin):
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Android\Android Studio\jbr", "Machine")
```

**Add to PATH**:
```powershell
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

### Run Tests via Gradle

Once Java is configured:

```powershell
# IMPORTANT: Navigate to the Android app directory (contains gradlew.bat)
cd D:\Projects\Software\Suled\suled-mobile\android\app

# Run all unit tests
.\gradlew.bat test

# Run with detailed output
.\gradlew.bat test --info

# Run debug variant tests specifically
.\gradlew.bat testDebugUnitTest

# Run with build first (recommended if code changed)
.\gradlew.bat clean testDebugUnitTest

# Generate test report
.\gradlew.bat test
# Then open: app\build\reports\tests\testDebugUnitTest\index.html
```

**Note:** The `--tests` filter option may not work with all Gradle versions. If you get "Unknown command-line option '--tests'", just run `.\gradlew.bat test` to run all tests.

### Run Instrumented Tests (Requires Emulator/Device)

```powershell
# Start an emulator or connect a device first
.\gradlew.bat connectedAndroidTest

# Run specific UI test
.\gradlew.bat connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.suled.app.ui.screens.PairSelectionScreenTest
```

## Option 3: Use Terminal in Android Studio

1. Open Android Studio
2. Click **Terminal** tab at bottom
3. The terminal will have Java/Gradle preconfigured
4. Run: `./gradlew test`

## Test Structure

```
📦 Created Tests (48 total)
├── 📂 Unit Tests (src/test/) - Fast, no device needed
│   ├── ✅ PairSelectionViewModelTest.kt (10 tests)
│   ├── ✅ GamesViewModelTest.kt (10 tests)
│   └── ✅ TournamentRepositoryTest.kt (19 tests)
│
└── 📂 Instrumented Tests (src/androidTest/) - Requires device/emulator
    └── ✅ PairSelectionScreenTest.kt (9 tests)
```

## Verifying Test Files Exist

Run this to list all test files:

```powershell
Get-ChildItem -Path "app\src" -Recurse -Filter "*Test.kt" | Select-Object FullName
```

Expected output:
- `PairSelectionViewModelTest.kt`
- `GamesViewModelTest.kt`
- `TournamentRepositoryTest.kt`
- `PairSelectionScreenTest.kt`

## Expected Test Results

### All Tests Should Pass ✅

When you run the tests, you should see:
```
> Task :app:testDebugUnitTest

com.suled.app.viewmodel.PairSelectionViewModelTest > initial state is loading PASSED
com.suled.app.viewmodel.PairSelectionViewModelTest > loadPairs updates state with pairs on success PASSED
... (48 tests total)

BUILD SUCCESSFUL in 15s
```

## Troubleshooting

### "JAVA_HOME is not set"
- Install JDK 17
- Set JAVA_HOME environment variable
- Or use Android Studio's built-in terminal

### "Cannot find gradlew"
- Make sure you're in `suled-mobile/android/app` directory
- On Windows use `.\gradlew.bat`
- On Mac/Linux use `./gradlew`

### "Tests not found"
- Rebuild project: `.\gradlew.bat clean build`
- Sync Gradle files in Android Studio
- Verify test files exist in correct directories

### "Module not specified"
- The main app module is `app`
- Test task: `:app:test`

### Gradle sync fails
1. Open in Android Studio
2. File → Sync Project with Gradle Files
3. Wait for sync to complete
4. Try running tests again

## Quick Verification

Run this to verify everything is set up:

```powershell
# In Android Studio Terminal or after setting up Java:
.\gradlew.bat tasks --group=verification
```

You should see:
- `test` - Runs unit tests
- `connectedAndroidTest` - Runs instrumented tests
- `check` - Runs all checks and tests

## What Each Test Does

### PairSelectionViewModelTest
- ✅ Tests loading state management
- ✅ Tests successful pair loading
- ✅ Tests error handling
- ✅ Tests retry functionality

### GamesViewModelTest
- ✅ Tests game loading for pairs
- ✅ Tests error handling
- ✅ Tests state transitions
- ✅ Tests retry functionality

### TournamentRepositoryTest
- ✅ Tests API integration
- ✅ Tests HTTP error codes (404, 500)
- ✅ Tests network failures
- ✅ Tests JSON parsing

### PairSelectionScreenTest
- ✅ Tests UI rendering
- ✅ Tests user interactions
- ✅ Tests different UI states

## Next Steps After Running Tests

1. ✅ All tests pass → Great! Your code is working
2. ❌ Some tests fail → Check the error messages
3. 📊 View coverage → Run with JaCoCo plugin
4. 🔄 Add more tests → Follow the patterns in existing tests

## Pro Tips

- Run tests frequently during development
- Use Android Studio for best experience
- Keep tests fast (unit tests < 1s each)
- Use descriptive test names
- Test edge cases and errors, not just happy paths

---

**Recommended**: Use Android Studio's test runner for the best experience!
