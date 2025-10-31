# Suled Mobile Apps

Mobile applications for Suled tournament management system for Android and iOS platforms.

## Related Repositories

- **Backend API**: [suled-backend](../suled-backend) - Azure Functions backend
- **Main Repository**: [Suled](../Suled) - Original monorepo (deprecated)

## Project Structure

```
suled-mobile/
├── .github/
│   └── workflows/              # CI/CD pipelines
│       ├── android-ci.yml      # Android build & deploy
│       └── ios-ci.yml          # iOS build & deploy
├── android/
│   ├── app/                    # Android phone app
│   │   ├── src/
│   │   │   └── main/
│   │   │       ├── java/com/suled/app/
│   │   │       └── res/
│   │   └── build.gradle.kts
│   └── wear/                   # Wear OS watch app
│       └── src/
├── ios/
│   ├── SuledApp/               # iOS phone app
│   │   ├── SuledApp/
│   │   │   ├── Views/
│   │   │   ├── ViewModels/
│   │   │   └── Models/
│   │   └── SuledApp.xcodeproj
│   └── SuledWatch/             # watchOS app
│       └── SuledWatch WatchKit App/
├── shared/                     # Kotlin Multiplatform shared code
│   ├── commonMain/
│   │   ├── models/             # Shared data models
│   │   ├── api/                # API client
│   │   └── repository/         # Data layer
│   ├── androidMain/            # Android-specific code
│   └── iosMain/                # iOS-specific code
└── docs/                       # Documentation
```

## Supported Platforms

- ✅ **Android 8.0+ (API 26+)** - Phone
- ✅ **Wear OS 3.0+** - Smart watches
- 🚧 **iOS 14.0+** - iPhone (Coming soon)
- 🚧 **watchOS 7.0+** - Apple Watch (Coming soon)

## Features

### Current Features (Android)
- ✅ View tournament pairs
- ✅ View games for selected pair
- ✅ Filter "Our Games" vs all games
- ✅ Material Design 3 UI
- ✅ Offline support (planned)

### Upcoming Features
- 🚧 Real-time score updates
- 🚧 Push notifications for game times
- 🚧 Court navigation/maps
- 🚧 Player statistics
- 🚧 Wear OS complications
- 🚧 iOS version
- 🚧 Kotlin Multiplatform shared code

## Getting Started

### Prerequisites

#### For Android Development
- [Android Studio Hedgehog | 2023.1.1+](https://developer.android.com/studio)
- JDK 17+
- Android SDK API 34
- Gradle 8.0+

#### For iOS Development
- macOS with Xcode 15+
- iOS Simulator or physical device
- CocoaPods (if using)

### Android Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd suled-mobile
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to `suled-mobile/android/app`

3. **Configure API endpoint**
   
   Update `android/app/src/main/java/com/suled/app/data/api/ApiConstants.kt`:
   ```kotlin
   object ApiConstants {
       const val BASE_URL = "https://your-api.azurewebsites.net/api/"
   }
   ```

4. **Build and run**
   ```bash
   cd android/app
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

   Or use Android Studio: Run > Run 'app'

### iOS Setup (Coming Soon)

1. **Open in Xcode**
   ```bash
   cd ios/SuledApp
   open SuledApp.xcodeproj
   ```

2. **Install dependencies** (if using CocoaPods)
   ```bash
   cd ios
   pod install
   open SuledApp.xcworkspace
   ```

3. **Configure API endpoint**
   
   Update configuration in project settings or Constants file.

4. **Build and run**
   - Select a simulator or device
   - Press Cmd+R to build and run

## Architecture

### Android
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose
- **Networking**: Ktor Client / Retrofit
- **Dependency Injection**: Hilt (planned)
- **Navigation**: Jetpack Navigation Compose
- **State Management**: ViewModel + StateFlow

### iOS (Planned)
- **Architecture**: MVVM
- **UI Framework**: SwiftUI
- **Networking**: URLSession / Alamofire
- **Dependency Injection**: Custom / Swinject
- **State Management**: Combine / @StateObject

### Shared Code (Kotlin Multiplatform - Planned)
```kotlin
shared/
├── commonMain/
│   ├── models/
│   │   ├── Game.kt           # Shared data models
│   │   └── Pair.kt
│   ├── api/
│   │   └── TournamentApi.kt  # API client interface
│   └── repository/
│       └── TournamentRepository.kt
├── androidMain/              # Android-specific implementations
└── iosMain/                  # iOS-specific implementations
```

## API Integration

The mobile apps communicate with the backend API deployed on Azure Functions.

### Backend Endpoints

**Production**: `https://suled-app-func.azurewebsites.net/api/`

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/pairs` | GET | Get all tournament pairs |
| `/api/games/pair/{pairId}` | GET | Get games for specific pair |

### Local Development

When running the backend locally (Azure Functions Core Tools):

**Android Emulator**: Use `http://10.0.2.2:7071/api/`  
**iOS Simulator**: Use `http://localhost:7071/api/`  
**Physical Device**: Use `http://YOUR_COMPUTER_IP:7071/api/`

To find your computer's IP:
```bash
# Windows PowerShell
ipconfig

# macOS/Linux
ifconfig
```

### Switching Environments

The Android app automatically uses the correct endpoint based on build type:
- **Debug builds**: Local backend (`http://10.0.2.2:7071/api/`)
- **Release builds**: Azure production (`https://suled-app-func.azurewebsites.net/api/`)

To manually override, edit `ApiConstants.kt` or `build.gradle.kts`.

## Development

### Running Tests

**Android:**
```bash
cd android/app
./gradlew test              # Unit tests
./gradlew connectedTest     # Instrumented tests
```

**iOS:**
```bash
cd ios
xcodebuild test -scheme SuledApp -destination 'platform=iOS Simulator,name=iPhone 15'
```

### Code Formatting

**Android:**
```bash
./gradlew ktlintFormat
```

**iOS:**
```bash
# Use SwiftFormat or SwiftLint
swiftformat .
swiftlint autocorrect
```

### Building Release Versions

**Android APK:**
```bash
cd android/app
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

**Android App Bundle (for Play Store):**
```bash
./gradlew bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

**iOS Archive:**
```bash
cd ios
xcodebuild -scheme SuledApp -configuration Release archive -archivePath build/SuledApp.xcarchive
```

## Deployment

### Android - Google Play Store

The CI/CD pipeline automatically builds and deploys to Play Store internal track when pushing to `main`.

**Manual deployment:**
```bash
# Upload to Play Console
./gradlew publishReleaseBundle
```

Required secrets:
- `KEYSTORE_BASE64` - Your signing keystore (base64 encoded)
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias
- `KEY_PASSWORD` - Key password
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` - Service account for Play Console

### iOS - TestFlight / App Store

The CI/CD pipeline automatically uploads to TestFlight when pushing to `main`.

Required secrets:
- `BUILD_CERTIFICATE_BASE64` - Apple distribution certificate
- `P12_PASSWORD` - Certificate password
- `PROVISIONING_PROFILE_BASE64` - Provisioning profile
- `APP_STORE_CONNECT_API_KEY_ID` - App Store Connect API key
- `APP_STORE_CONNECT_ISSUER_ID` - Issuer ID
- `APP_STORE_CONNECT_API_KEY_BASE64` - API key file

## Troubleshooting

### Android

**Build fails with "SDK not found":**
```bash
# Set ANDROID_HOME environment variable
export ANDROID_HOME=$HOME/Android/Sdk
```

**Gradle sync issues:**
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

**API calls fail:**
- Check `ApiConstants.BASE_URL` is correct
- Verify network permissions in `AndroidManifest.xml`
- Check backend API is running

### iOS

**CocoaPods issues:**
```bash
cd ios
pod deintegrate
pod install
```

**Code signing errors:**
- Verify provisioning profiles in Xcode
- Check bundle identifier matches your Apple Developer account

## Contributing

1. Create a feature branch from `develop`
2. Make your changes
3. Write/update tests
4. Submit a pull request
5. Ensure CI passes

## Design Guidelines

### Android
- Follow [Material Design 3](https://m3.material.io/) guidelines
- Use Jetpack Compose best practices
- Support dark mode
- Ensure accessibility (TalkBack, large text)

### iOS
- Follow [Human Interface Guidelines](https://developer.apple.com/design/human-interface-guidelines/)
- Use native iOS components
- Support dark mode
- Ensure accessibility (VoiceOver, Dynamic Type)

## Roadmap

### Phase 1: Foundation ✅
- [x] Android phone app
- [x] Basic UI with Jetpack Compose
- [x] API integration

### Phase 2: Enhancement 🚧
- [ ] Kotlin Multiplatform shared code
- [ ] Offline support with local database
- [ ] Real-time updates
- [ ] Push notifications

### Phase 3: Expansion 📅
- [ ] iOS phone app
- [ ] Wear OS watch app
- [ ] Apple Watch app
- [ ] Tablet optimizations

### Phase 4: Advanced Features 🔮
- [ ] Court maps and navigation
- [ ] Player statistics and analytics
- [ ] Social features (team chat)
- [ ] Tournament brackets visualization

## License

[Your License Here]

## Support

For issues or questions:
- Create an issue in this repository
- Contact: [your-email]
