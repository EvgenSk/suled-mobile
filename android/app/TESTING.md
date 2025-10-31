# Android Testing Suite

Complete test suite for the Suled Android application following testing best practices.

## Test Structure

```
app/src/
├── test/                          # Unit tests (JVM)
│   └── java/com/suled/app/
│       ├── viewmodel/
│       │   ├── PairSelectionViewModelTest.kt
│       │   └── GamesViewModelTest.kt
│       ├── repository/
│       │   └── TournamentRepositoryTest.kt
│       └── helpers/
│           ├── TestData.kt
│           └── CoroutineTestRule.kt
└── androidTest/                   # Instrumented tests (Android device)
    └── java/com/suled/app/
        ├── ui/screens/
        │   └── PairSelectionScreenTest.kt
        └── helpers/
            └── TestData.kt
```

## Test Coverage

### ✅ Unit Tests (70% of testing pyramid)

#### PairSelectionViewModelTest
- ✅ Initial loading state
- ✅ Successful pair loading
- ✅ Error handling
- ✅ Empty response handling
- ✅ Retry functionality
- ✅ Loading state management
- ✅ Multiple calls handling

**Coverage**: 10 test cases

#### GamesViewModelTest
- ✅ Initial empty state
- ✅ Successful games loading
- ✅ Loading state management
- ✅ Error handling
- ✅ Empty games list handling
- ✅ Retry functionality
- ✅ Different pairs handling
- ✅ Our games filtering

**Coverage**: 10 test cases

#### TournamentRepositoryTest (Integration)
- ✅ Successful API responses
- ✅ Empty responses
- ✅ 404 error handling
- ✅ 500 error handling
- ✅ Network error handling
- ✅ Malformed JSON handling
- ✅ Timeout handling
- ✅ API request validation
- ✅ Data parsing (isOurGame, scheduledTime)
- ✅ Sequential requests

**Coverage**: 19 test cases

### ✅ UI Tests (10% of testing pyramid)

#### PairSelectionScreenTest
- ✅ Loading indicator display
- ✅ Loaded pairs display
- ✅ Error message display
- ✅ Empty state display
- ✅ Pair click callback
- ✅ Refresh button functionality
- ✅ Retry button functionality
- ✅ Correct title display
- ✅ Multiple pairs scrolling

**Coverage**: 9 test cases

## Running Tests

### Run All Unit Tests
```bash
./gradlew test
```

### Run Unit Tests with Coverage
```bash
./gradlew testDebugUnitTest --tests "*"
```

### Run Specific Test Class
```bash
./gradlew test --tests "com.suled.app.viewmodel.GamesViewModelTest"
```

### Run All Instrumented Tests
```bash
./gradlew connectedAndroidTest
```

### Run Specific UI Test
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.suled.app.ui.screens.PairSelectionScreenTest
```

### Run Tests in Android Studio
1. Right-click on test file or folder
2. Select "Run Tests"
3. View results in the "Run" panel

## Test Dependencies

```kotlin
// Unit Testing
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
testImplementation("io.mockk:mockk:1.13.8")
testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
testImplementation("app.cash.turbine:turbine:1.0.0")
testImplementation("com.google.truth:truth:1.1.5")

// Android Instrumented Testing
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
androidTestImplementation("io.mockk:mockk-android:1.13.8")
```

## Test Utilities

### TestData Factory
Provides factory methods for creating test data:
- `createPair()` - Single pair
- `createPairs(count)` - Multiple pairs
- `createGame()` - Single game
- `createGames(count, ourGames)` - Multiple games
- `Json.pairsResponse()` - JSON response for MockWebServer
- `Json.gamesResponse()` - JSON response for MockWebServer

### CoroutineTestRule
JUnit rule for coroutine testing:
```kotlin
@get:Rule
val coroutineRule = CoroutineTestRule()
```

## Refactoring for Testability

### Before (Not Testable)
```kotlin
class GamesViewModel : ViewModel() {
    private val repository = TournamentRepository() // ❌ Can't mock
}
```

### After (Testable)
```kotlin
class GamesViewModel(
    private val repository: TournamentRepository = TournamentRepository() // ✅ Can inject mock
) : ViewModel()
```

### Before (Hard to Test)
```kotlin
@Composable
fun PairSelectionScreen(viewModel: PairSelectionViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState() // ❌ Requires mocking ViewModel
}
```

### After (Easy to Test)
```kotlin
@Composable
fun PairSelectionScreenContent(
    uiState: PairSelectionUiState, // ✅ Direct state injection
    onPairClick: (String, String) -> Unit,
    onRetry: () -> Unit
)
```

## Testing Patterns

### Given-When-Then
```kotlin
@Test
fun `loadPairs updates state with pairs on success`() = runTest {
    // Given - Setup test data and mocks
    val mockPairs = TestData.createPairs(3)
    coEvery { repository.getPairs() } returns Result.success(mockPairs)
    
    // When - Execute the action
    viewModel = PairSelectionViewModel(repository)
    advanceUntilIdle()
    
    // Then - Verify the result
    assertEquals(3, viewModel.uiState.value.pairs.size)
    assertFalse(viewModel.uiState.value.isLoading)
}
```

### Testing Flows with Turbine
```kotlin
viewModel.uiState.test {
    val state = awaitItem()
    assertEquals(expectedValue, state.something)
}
```

### Testing Coroutines
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
@Test
fun testSuspendFunction() = runTest {
    // Test code with suspend functions
    advanceUntilIdle() // Process all pending coroutines
}
```

### MockWebServer Pattern
```kotlin
@Before
fun setup() {
    mockWebServer = MockWebServer()
    mockWebServer.start()
    // Setup Retrofit with mockWebServer.url()
}

@Test
fun testApiCall() = runTest {
    mockWebServer.enqueue(MockResponse()
        .setResponseCode(200)
        .setBody(testJson))
    
    val result = repository.getData()
    assertTrue(result.isSuccess)
}
```

## Best Practices Applied

✅ **Dependency Injection** - ViewModels and Repository accept dependencies  
✅ **Separation of Concerns** - UI state management separated from business logic  
✅ **Test Data Factories** - Reusable test data creation  
✅ **Coroutine Testing** - Proper test dispatcher setup  
✅ **Mock External Dependencies** - API calls mocked with MockWebServer  
✅ **Descriptive Test Names** - Using backticks for readable names  
✅ **Comprehensive Coverage** - Testing success, error, and edge cases  
✅ **Fast Tests** - Unit tests run in milliseconds  
✅ **Isolated Tests** - Each test is independent  
✅ **Testable UI** - Composables accept state directly  

## Code Coverage Goals

- **ViewModels**: 100% (business logic)
- **Repository**: 90%+ (API integration)
- **UI Composables**: 80%+ (critical paths)
- **Overall**: 80%+ line coverage

## CI/CD Integration

Tests should run automatically on:
- Every push to develop/main
- Every pull request
- Pre-merge validation

Example GitHub Actions:
```yaml
- name: Run Unit Tests
  run: ./gradlew test
  
- name: Run Integration Tests  
  run: ./gradlew connectedAndroidTest
```

## Next Steps

1. ✅ Add tests for additional screens (GamesListScreen)
2. ✅ Add end-to-end tests for navigation flows
3. ✅ Set up code coverage reporting (JaCoCo)
4. ✅ Add performance tests
5. ✅ Add screenshot tests (if needed)

## Common Issues & Solutions

### Issue: Tests fail with "Job was cancelled"
**Solution**: Use `runTest` and `advanceUntilIdle()`

### Issue: Flow tests don't capture emissions
**Solution**: Use Turbine library: `flow.test { awaitItem() }`

### Issue: UI tests can't find composables
**Solution**: Use semantic properties and test tags

### Issue: MockWebServer responses not working
**Solution**: Ensure JSON format matches API response exactly

## Resources

- [Android Testing Guide](https://developer.android.com/training/testing)
- [Kotlin Coroutines Testing](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [MockK Documentation](https://mockk.io/)
- [Turbine Documentation](https://github.com/cashapp/turbine)
