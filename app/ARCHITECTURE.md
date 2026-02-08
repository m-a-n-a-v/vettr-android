# VETTR Android - Architecture Documentation

## Overview

VETTR Android is built using modern Android development best practices with a focus on maintainability, testability, and scalability. This document outlines the key architectural patterns and decisions used throughout the application.

## Architecture Patterns

### MVVM (Model-View-ViewModel)

The app follows the **Model-View-ViewModel (MVVM)** pattern for UI architecture:

- **Model**: Data layer with Room entities, repositories, and data sources
- **View**: Jetpack Compose UI components (screens and composables)
- **ViewModel**: `@HiltViewModel` classes that manage UI state and business logic

**Example:**
```kotlin
@HiltViewModel
class PulseViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PulseUiState())
    val uiState: StateFlow<PulseUiState> = _uiState.asStateFlow()

    // Business logic here
}
```

**Benefits:**
- Separation of concerns between UI and business logic
- Testable ViewModels independent of Android framework
- Reactive UI updates through StateFlow

### Repository Pattern

The **Repository Pattern** provides a clean abstraction over data sources:

- **Interface**: Defines contract for data operations (e.g., `StockRepository`)
- **Implementation**: Handles data fetching from local (Room) and remote (Retrofit) sources
- **Single Source of Truth**: Local database is the source of truth; remote data syncs to local

**Example:**
```kotlin
interface StockRepository {
    fun getStocks(): Flow<List<Stock>>
    suspend fun toggleFavorite(stockId: String)
}

class StockRepositoryImpl @Inject constructor(
    private val stockDao: StockDao,
    private val api: VettrApi
) : StockRepository {
    override fun getStocks(): Flow<List<Stock>> = stockDao.getAll()

    override suspend fun toggleFavorite(stockId: String) {
        stockDao.toggleFavorite(stockId)
    }
}
```

**Benefits:**
- Abstracts data source details from ViewModels
- Easy to mock for testing
- Centralizes data access logic

### Dependency Injection with Hilt

The app uses **Hilt** for dependency injection throughout:

- **@HiltAndroidApp**: Application class annotation
- **@AndroidEntryPoint**: Activity/Fragment annotation
- **@HiltViewModel**: ViewModel annotation with constructor injection
- **@Module + @InstallIn**: Hilt modules for providing dependencies

**Example:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VettrDatabase {
        return Room.databaseBuilder(context, VettrDatabase::class.java, "vettr-db").build()
    }

    @Provides
    fun provideStockDao(database: VettrDatabase): StockDao = database.stockDao()
}
```

**Benefits:**
- Compile-time dependency validation
- Reduces boilerplate compared to manual DI
- Easy scoping (Singleton, ActivityScoped, etc.)

### Navigation with Navigation Compose

The app uses **Navigation Compose** for type-safe navigation:

- **Sealed class routes**: Type-safe route definitions
- **NavHost**: Central navigation graph in `MainActivity`
- **Bottom navigation**: Five tabs (Pulse, Discovery, Stocks, Alerts, Profile)

**Example:**
```kotlin
sealed class Screen(val route: String) {
    object Pulse : Screen("pulse")
    object Discovery : Screen("discovery")
    object StockDetail : Screen("stock/{ticker}") {
        fun createRoute(ticker: String) = "stock/$ticker"
    }
}

NavHost(navController = navController, startDestination = Screen.Pulse.route) {
    composable(Screen.Pulse.route) { PulseScreen() }
    composable(Screen.StockDetail.route) { backStackEntry ->
        val ticker = backStackEntry.arguments?.getString("ticker")
        StockDetailScreen(ticker = ticker)
    }
}
```

**Benefits:**
- Type-safe navigation with compile-time checking
- Clear navigation structure
- Deep linking support

## Data Layer Architecture

### Room Database

The app uses **Room** for local data persistence:

- **@Entity**: Data models (Stock, User, Filing, etc.)
- **@Dao**: Data access objects with suspend functions
- **@Database**: Single database class with type converters

**Key Features:**
- Reactive queries using `Flow` for real-time UI updates
- Foreign key relationships (e.g., Filing → Stock)
- Indices for optimized queries
- OnConflict strategies for upsert operations

**Example:**
```kotlin
@Entity(tableName = "stocks")
data class Stock(
    @PrimaryKey val id: String,
    val ticker: String,
    val name: String,
    @ColumnInfo(name = "vetr_score") val vetrScore: Int
)

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks")
    fun getAll(): Flow<List<Stock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<Stock>)
}
```

### Retrofit for API Calls

The app uses **Retrofit** for network operations:

- **Suspend functions**: Coroutine-based async operations
- **OkHttp interceptors**: Authentication, logging
- **Converters**: Gson for JSON parsing

**Example:**
```kotlin
interface VettrApi {
    @GET("stocks")
    suspend fun getStocks(): List<StockDto>

    @GET("stocks/{ticker}")
    suspend fun getStockDetails(@Path("ticker") ticker: String): StockDto
}
```

### DataStore for Preferences

**DataStore Preferences** is used for simple key-value storage:

- Onboarding completion state
- User settings (currency, notifications, etc.)
- Reactive `Flow` for observing changes

## UI Layer Architecture

### Jetpack Compose

The entire UI is built with **Jetpack Compose**:

- **Declarative UI**: UI as a function of state
- **State hoisting**: State managed in ViewModels, passed to composables
- **Reusable components**: Shared design system components

**Example:**
```kotlin
@Composable
fun PulseScreen(
    viewModel: PulseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Pulse") }) }
    ) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(uiState.stocks) { stock ->
                StockCard(stock = stock)
            }
        }
    }
}
```

### State Management

State management follows these principles:

- **StateFlow in ViewModels**: `MutableStateFlow` for internal state, exposed as `StateFlow`
- **collectAsStateWithLifecycle()**: Lifecycle-aware state collection in Compose
- **Immutable state classes**: Data classes for UI state
- **Single source of truth**: ViewModel is the source of truth for UI state

**Example:**
```kotlin
data class PulseUiState(
    val stocks: List<Stock> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// In ViewModel:
private val _uiState = MutableStateFlow(PulseUiState())
val uiState: StateFlow<PulseUiState> = _uiState.asStateFlow()

fun loadData() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }
        // Load data...
        _uiState.update { it.copy(stocks = loadedStocks, isLoading = false) }
    }
}
```

### Design System

The app uses a centralized **design system**:

- **Theme.kt**: Material 3 theme with custom color scheme
- **Color.kt**: Semantic color tokens (VettrNavy, VettrGreen, etc.)
- **Type.kt**: Typography scale using Montserrat font
- **Reusable components**: Shared UI components in `designsystem/components/`

**Benefits:**
- Consistent visual design across the app
- Easy to update design tokens globally
- Type-safe access to colors and typography

## Business Logic Layer

### VETR Score Calculation

The **VetrScoreCalculator** implements the proprietary scoring algorithm:

- **Weighted components**: Pedigree (25%), Filing Velocity (20%), Red Flags (25%), Growth (15%), Governance (15%)
- **Thread-safe caching**: Mutex-protected cache with 24-hour TTL
- **Detailed breakdown**: Returns overall score plus component scores

### Red Flag Detection

The **RedFlagDetector** analyzes stocks for warning signs:

- **Five flag types**: Consolidation velocity, financing velocity, executive churn, disclosure gaps, debt trend
- **Weighted scoring**: Each flag type has a specific weight contributing to composite score
- **Severity levels**: Low (<30), Moderate (30-60), High (60-85), Critical (>85)

## Coroutines and Threading

The app uses **Kotlin Coroutines** for asynchronous operations:

- **viewModelScope**: For ViewModel operations, canceled when ViewModel is cleared
- **Dispatchers.IO**: For database and network operations
- **Flow**: For reactive data streams from Room and repositories
- **Mutex**: For thread-safe operations in calculators and detectors

**Example:**
```kotlin
fun loadStocks() {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true) }

        stockRepository.getStocks()
            .catch { error -> _uiState.update { it.copy(error = error.message) } }
            .collect { stocks -> _uiState.update { it.copy(stocks = stocks, isLoading = false) } }
    }
}
```

## Testing Strategy

The architecture is designed for testability:

- **Repository interfaces**: Easy to mock for ViewModel tests
- **Dependency injection**: All dependencies injected, no hardcoded dependencies
- **Pure functions**: Business logic isolated in calculators/detectors
- **Reactive streams**: Easy to test Flow emissions

**Example test structure:**
```kotlin
class PulseViewModelTest {
    private lateinit var viewModel: PulseViewModel
    private val stockRepository: StockRepository = mockk()

    @Test
    fun `loadStocks updates uiState with stocks`() = runTest {
        // Arrange
        val mockStocks = listOf(Stock(...))
        every { stockRepository.getStocks() } returns flowOf(mockStocks)

        // Act
        viewModel = PulseViewModel(stockRepository)
        advanceUntilIdle()

        // Assert
        assertEquals(mockStocks, viewModel.uiState.value.stocks)
    }
}
```

## Key Architectural Decisions

### 1. Single Activity Architecture
- **Decision**: Use single `MainActivity` with Navigation Compose
- **Rationale**: Simpler navigation, better state preservation, aligns with modern Android best practices

### 2. Room as Single Source of Truth
- **Decision**: Local database is the authoritative data source
- **Rationale**: Works offline, faster UI updates, syncs asynchronously with backend

### 3. Compose over XML
- **Decision**: Use Jetpack Compose for all UI
- **Rationale**: Modern declarative paradigm, better performance, less boilerplate

### 4. Hilt over Dagger
- **Decision**: Use Hilt for dependency injection
- **Rationale**: Less boilerplate, better Android integration, compile-time validation

### 5. StateFlow over LiveData
- **Decision**: Use StateFlow for ViewModel state
- **Rationale**: Better Compose integration, Kotlin-first, more powerful operators

## Module Structure

```
app/src/main/java/com/vettr/android/
├── VettrApp.kt                  # Application class
├── MainActivity.kt              # Single activity host
├── navigation/                  # Navigation graph
├── core/                        # Core infrastructure
│   ├── model/                   # Room entities
│   ├── data/
│   │   ├── local/              # DAOs
│   │   └── repository/         # Repository implementations
│   ├── di/                      # Hilt modules
│   └── util/                    # Services, extensions
├── feature/                     # Feature modules
│   ├── auth/                    # Authentication
│   ├── pulse/                   # Pulse screen
│   ├── discovery/               # Discovery screen
│   ├── stockdetail/             # Stock detail screens
│   ├── alerts/                  # Alerts screen
│   └── profile/                 # Profile/settings
└── designsystem/                # Design system
    ├── theme/                   # Theme, colors, typography
    └── components/              # Reusable UI components
```

## Data Flow

1. **User interaction** → Composable calls ViewModel function
2. **ViewModel** → Calls repository method
3. **Repository** → Fetches from Room (local) or Retrofit (remote)
4. **Room/Retrofit** → Returns data via Flow or suspend function
5. **Repository** → Transforms/caches data, emits via Flow
6. **ViewModel** → Updates StateFlow with new data
7. **Composable** → Recomposes with new state

## Performance Considerations

- **Caching**: Score calculations cached for 24 hours
- **Pagination**: Stock lists support pagination to reduce memory usage
- **Lazy loading**: LazyColumn/LazyRow for efficient list rendering
- **Debouncing**: Search and refresh operations debounced to prevent excessive calls
- **Background processing**: Heavy calculations run on Dispatchers.IO

## Security Considerations

- **Biometric authentication**: Uses BiometricPrompt API with BIOMETRIC_STRONG
- **No PII logging**: Analytics service sanitizes personally identifiable information
- **Secure storage**: Auth tokens stored in encrypted DataStore
- **HTTPS only**: All network calls use HTTPS

## Observability

- **ObservabilityService**: Tracks screen load times, error rates, user actions
- **SyncHistory**: Tracks all sync operations for debugging
- **Timber logging**: Structured logging with tags for filtering

## Future Improvements

- **Multi-module architecture**: Split into feature modules for better build times
- **Offline-first sync**: Implement robust sync conflict resolution
- **GraphQL**: Replace REST API with GraphQL for efficient data fetching
- **Compose multiplatform**: Share UI code with iOS
