# Suled - Suled Android App

Android application for viewing tournament games and schedules built with Kotlin and Jetpack Compose.

## Features

- ✅ View all available pairs
- ✅ Select your pair
- ✅ See upcoming games for your pair
- ✅ View game details (court number, opponents, round, status)
- ✅ Material Design 3 UI
- ✅ Offline error handling
- ✅ Pull-to-refresh functionality

## Tech Stack

- **Language**: Kotlin 1.9.20
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Async**: Kotlin Coroutines + Flow
- **Navigation**: Jetpack Navigation Compose
- **Dependency Injection**: Constructor injection (simple approach)

## Prerequisites

**Option 1: Android Studio (Recommended)**
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or higher
- Android SDK API 34
- Kotlin 1.9.20+
- Minimum device: Android 8.0 (API 26)

**Option 2: IntelliJ IDEA Ultimate**
- IntelliJ IDEA Ultimate (Community Edition does NOT support Android)
- Android Plugin installed
- Android SDK configured manually
- JDK 17 or higher

**See [INTELLIJ_SETUP.md](INTELLIJ_SETUP.md) for detailed IntelliJ IDEA setup instructions.**

## Setup Instructions

### 1. Configure Backend URL

The app automatically uses the correct backend URL based on build type:

**Debug builds** (local development):
- Android Emulator: `http://10.0.2.2:7071/api/`
- Physical Device: Update `build.gradle.kts` with your computer's IP

**Release builds** (production):
- Azure Functions: `https://suled-app-func.azurewebsites.net/api/`

To manually override, edit [`ApiConstants.kt`](app/src/main/java/com/suled/app/data/api/ApiConstants.kt) and [`build.gradle.kts`](app/build.gradle.kts):

```kotlin
// In build.gradle.kts
buildTypes {
    debug {
        buildConfigField("String", "API_BASE_URL", "\"http://YOUR_IP:7071/api/\"")
    }
    release {
        buildConfigField("String", "API_BASE_URL", "\"https://suled-app-func.azurewebsites.net/api/\"")
    }
}
```

**Find your computer's IP:**
```powershell
# Windows PowerShell
ipconfig

# macOS/Linux
ifconfig
```

### 2. Open Project

**Android Studio:**
1. Open Android Studio
2. Select **File > Open**
3. Navigate to `mobile/SuledApp` directory
4. Click **OK**
5. Wait for Gradle sync to complete

**IntelliJ IDEA Ultimate:**
1. Follow setup instructions in [INTELLIJ_SETUP.md](INTELLIJ_SETUP.md)
2. Open project from `mobile/SuledApp`
3. Wait for Gradle sync

### 3. Build the App

#### For Development (Debug)
```powershell
# Clean build
.\gradlew.bat clean

# Build debug APK
.\gradlew.bat assembleDebug

# Install on connected device
.\gradlew.bat installDebug

# Or use the build menu script
.\build-menu.ps1
```

#### For Release (Production)
See **[RELEASE_SETUP.md](RELEASE_SETUP.md)** for complete instructions on:
- Generating release keystore
- Configuring signing
- Building signed APK/AAB
- Publishing to Google Play Store

Quick commands (after keystore setup):
```powershell
# Build signed release APK
.\gradlew.bat assembleRelease

# Build signed AAB for Play Store
.\gradlew.bat bundleRelease
```

# Build release APK (requires signing configuration)
.\gradlew assembleRelease
```

### 4. Run on Emulator

1. Create an Android Virtual Device (AVD):
   - Tools > Device Manager > Create Device
   - Select a device (e.g., Pixel 5)
   - Select system image (API 34 recommended)
   - Click Finish

2. Run the app:
   - Press **Run** button or `Shift + F10`
   - Select your emulator
   - Wait for app to launch

### 5. Run on Physical Device

1. Enable Developer Options on your device:
   - Settings > About phone > Tap "Build number" 7 times

2. Enable USB Debugging:
   - Settings > Developer options > USB debugging

3. Connect device via USB

4. Run the app:
   - Press **Run** button
   - Select your device
   - Click **OK**

## Project Structure

```
app/src/main/java/com/suled/app/
├── data/
│   ├── api/
│   │   ├── TournamentApiService.kt    # Retrofit API interface
│   │   └── ApiConstants.kt            # API configuration
│   ├── models/
│   │   ├── Pair.kt                    # Pair data model
│   │   └── Game.kt                    # Game data model
│   └── repository/
│       └── TournamentRepository.kt    # Data access layer
├── ui/
│   ├── screens/
│   │   ├── PairSelectionScreen.kt     # Pair selection UI
│   │   └── GamesListScreen.kt         # Games list UI
│   ├── components/
│   │   ├── GameCard.kt                # Game card component
│   │   └── PairCard.kt                # Pair card component
│   ├── theme/
│   │   ├── Color.kt                   # App colors
│   │   ├── Theme.kt                   # Material theme
│   │   └── Type.kt                    # Typography
│   └── navigation/
│       └── NavGraph.kt                # Navigation graph
├── viewmodel/
│   ├── PairSelectionViewModel.kt      # Pair selection logic
│   └── GamesViewModel.kt              # Games list logic
└── MainActivity.kt                     # Main activity
```

## Testing

### Run Unit Tests

```bash
.\gradlew test
```

### Run Instrumented Tests

```bash
.\gradlew connectedAndroidTest
```

## Troubleshooting

### Gradle Sync Issues

1. **Clean and rebuild**:
   ```bash
   .\gradlew clean
   .\gradlew build
   ```

2. **Invalidate caches** (Android Studio/IntelliJ):
   - File > Invalidate Caches / Restart
   - Click "Invalidate and Restart"

3. **Update Gradle wrapper**:
   ```bash
   .\gradlew wrapper --gradle-version 8.2
   ```

### Network Connection Issues

1. **Check backend is running**:
   ```bash
   cd backend/TournamentFunctions
   func start
   ```

2. **Verify API URL** in `ApiConstants.kt`

3. **Check firewall settings** (for local backend)

4. **Enable clear text traffic** (already configured in AndroidManifest.xml)

### Emulator Issues

1. **API endpoint**: Use `http://10.0.2.2:7071/api/` for local backend

2. **Network proxy**: Disable any proxy settings in emulator

3. **Wipe data**: Tools > Device Manager > Wipe Data

### Physical Device Issues

1. **Find your computer's IP**:
   - Windows: `ipconfig`
   - macOS/Linux: `ifconfig` or `ip addr`

2. **Update BASE_URL** with your computer's IP:
   ```kotlin
   const val BASE_URL = "http://192.168.1.100:7071/api/"
   ```

3. **Ensure device and computer** are on the same network

## API Integration

The app connects to the Azure Functions backend:

### Endpoints Used

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/pairs` | GET | Fetch all available pairs |
| `/api/games/pair/{pairId}` | GET | Get games for specific pair |

### Response Models

**Pairs Response:**
```json
{
  "pairs": [
    {
      "id": "guid",
      "displayName": "Player1 & Player2",
      "player1": "Player1",
      "player2": "Player2"
    }
  ],
  "totalPairs": 10
}
```

**Games Response:**
```json
{
  "pairId": "guid",
  "games": [
    {
      "id": "guid",
      "round": 1,
      "courtNumber": 1,
      "status": "Scheduled",
      "pair1": "Player1 & Player2",
      "pair2": "Player3 & Player4",
      "isOurGame": true,
      "scheduledTime": null
    }
  ],
  "totalGames": 5
}
```

## Build Variants

- **debug** - Development build with logging enabled
- **release** - Production build (requires signing configuration)

## Next Steps

- [ ] Add authentication (Azure AD B2C)
- [ ] Implement push notifications
- [ ] Add game status updates
- [ ] Implement offline caching
- [ ] Add tournament selection
- [ ] Create smartwatch companion app
- [ ] Implement dark theme

## License

[Your License]

## Support

For issues and questions:
- Check backend is running at `http://localhost:7071`
- Verify network connectivity
- Check logs in Android Studio/IntelliJ Logcat
- See [INTELLIJ_SETUP.md](INTELLIJ_SETUP.md) for IntelliJ-specific issues
