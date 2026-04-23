# GitHub Copilot Instructions for Suled Mobile

## 🚨 CRITICAL WORKFLOW RULE
**ALWAYS run tests IMMEDIATELY after making code changes, especially refactoring.**
- Command: `./gradlew test` (from android/app directory)
- Do NOT report work as complete until tests pass
- This is a mandatory step, not optional

## Testing Guidelines

### When Business Logic Changes
- **Always update tests** when modifying ViewModels, repositories, data models, or API services
- Ensure test assertions match the new expected behavior
- Update test data/mocks (TestData.kt) to reflect structural changes
- Add new test cases for new functionality or edge cases
- Maintain both unit tests (JVM) and instrumented tests (Android device)

### After Refactoring
- **CRITICAL: IMMEDIATELY run tests after ANY refactoring** - this is non-negotiable
- **REQUIRED STEP**: Run unit tests: `./gradlew test` (from android/app directory)
- **REQUIRED STEP**: Run instrumented tests (if UI changed): `./gradlew connectedAndroidTest`
- **DO NOT** present work as complete until ALL tests pass
- Run specific test class if needed: `./gradlew test --tests "com.suled.app.viewmodel.GamesViewModelTest"`
- Fix any failing tests before considering the refactoring complete
- Ensure all tests pass before committing
- **WORKFLOW**: Code change → Run tests → Fix failures → Verify passing → THEN report complete

### Test Maintenance
- Keep test mocks synchronized with backend API responses
- When models change (e.g., Game, Pair, Tournament, TournamentRound), update all affected test files in both `test/` and `androidTest/`
- When adding new screens or ViewModels, create corresponding test coverage
- Verify ViewModels, repositories, UI screens, and edge cases
- Follow testing pyramid: 70% unit tests, 20% integration tests, 10% UI tests

## Architecture Notes
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with Jetpack Compose
- **Data Models**: Game, Pair, Tournament with API response wrappers
  - Backend uses pair-centered architecture: Tournament → TournamentPair → PairGame
  - Mobile API endpoints return flattened DTOs for simplicity
  - Pair model includes `gameCount` field from backend
- **Networking**: Retrofit + OkHttp with Kotlin Coroutines + Flow
- **UI Framework**: Jetpack Compose with Material 3
- **Dependency Injection**: Constructor injection (simple approach)
- Always maintain separation of concerns between UI, ViewModel, and Repository layers
- Use StateFlow for reactive state management in ViewModels
- Keep mobile models aligned with backend DTOs (PairDto, GameDto, TournamentListDto)

## Build Configuration
- Debug builds use local backend: `http://10.0.2.2:7071/api/` (emulator) or computer IP (physical device)
- Release builds use production: `https://suled-app-func.azurewebsites.net/api/`
- Build config is managed in `build.gradle.kts` with `buildConfigField`
