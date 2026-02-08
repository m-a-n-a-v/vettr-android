# VETTR Android - Ralph Agent Instructions

You are an autonomous coding agent working on the VETTR Android app - an intelligence platform for venture and micro-cap investors.

## Your Task

1. Read the PRD at `scripts/ralph/prd.json`
2. Read the progress log at `scripts/ralph/progress.txt` (check Codebase Patterns section first)
3. Check you're on the correct branch from PRD `branchName`. If not, check it out or create from main.
4. Pick the **highest priority** user story where `passes: false`
5. Implement that single user story
6. Run quality checks (see Quality Commands below)
7. Update CLAUDE.md files if you discover reusable patterns
8. If checks pass, commit ALL changes with message: `feat: [Story ID] - [Story Title]`
9. Update the PRD to set `passes: true` for the completed story
10. Append your progress to `scripts/ralph/progress.txt`

---

## Quality Commands

Execute these from the project root (`/Users/manav/Space/code/vettr-android`):

**Build:**
```bash
./gradlew assembleDebug
```

**Test:**
```bash
./gradlew testDebugUnitTest
```

**Lint (after lint story is complete):**
```bash
./gradlew detekt
```

All commits MUST pass the build command. Tests and lint will be added progressively.

---

## Folder Structure

```
app/src/main/java/com/vettr/android/
├── VettrApp.kt                     # @HiltAndroidApp Application class
├── MainActivity.kt                  # Single activity with Compose
├── navigation/
│   └── VettrNavGraph.kt            # Navigation routes + bottom nav
├── core/
│   ├── models/                     # Room @Entity classes
│   │   ├── Stock.kt
│   │   ├── User.kt
│   │   ├── Filing.kt
│   │   ├── Alert.kt
│   │   ├── AlertRule.kt
│   │   ├── Executive.kt
│   │   └── Watchlist.kt
│   ├── database/
│   │   ├── VettrDatabase.kt       # Room database
│   │   └── dao/                    # Data access objects
│   ├── services/
│   │   ├── auth/                   # Google Sign-In, BiometricPrompt
│   │   ├── networking/             # Retrofit + OkHttp
│   │   ├── sync/                   # WorkManager sync
│   │   └── mocks/                  # Mock implementations
│   ├── di/                         # Hilt modules
│   └── util/                       # Extensions, helpers
├── features/
│   ├── auth/
│   │   ├── AuthScreen.kt
│   │   └── AuthViewModel.kt
│   ├── pulse/
│   │   ├── PulseScreen.kt
│   │   └── PulseViewModel.kt
│   ├── discovery/
│   ├── stockdetail/
│   ├── pedigree/
│   ├── alerts/
│   └── profile/
└── designsystem/
    ├── theme/
    │   ├── Color.kt
    │   ├── Type.kt
    │   └── Theme.kt
    └── components/                 # Reusable @Composable functions
```

**File Naming:**
- Screens: `SomethingScreen.kt`
- ViewModels: `SomethingViewModel.kt`
- Composables: `SomethingComponent.kt` or just the composable name
- Entities: `Something.kt`
- DAOs: `SomethingDao.kt`

---

## Codebase Patterns

### ViewModels
- Always use `@HiltViewModel` with `@Inject constructor`
- Use `StateFlow` for UI state (NOT LiveData)
- Use `viewModelScope` for coroutines
- Example:
```kotlin
@HiltViewModel
class PulseViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PulseUiState())
    val uiState: StateFlow<PulseUiState> = _uiState.asStateFlow()

    fun fetchStocks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // ...
        }
    }
}

data class PulseUiState(
    val stocks: List<Stock> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Compose Screens
- Use `@Composable` functions, NOT classes
- Collect state with `collectAsStateWithLifecycle()`
- Use `@Preview` for all composables
- Example:
```kotlin
@Composable
fun PulseScreen(
    viewModel: PulseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // UI...
}

@Preview(showBackground = true)
@Composable
fun PulseScreenPreview() {
    VettrTheme {
        // ...
    }
}
```

### Room Database
- Entities use `@Entity` annotation
- DAOs use `@Dao` with suspend functions
- Database uses `@Database`
- Example:
```kotlin
@Entity(tableName = "stocks")
data class Stock(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val ticker: String,
    val name: String,
    val exchange: String,
    val sector: String,
    val marketCap: Double,
    val price: Double,
    val priceChange: Double,
    val vetrScore: Int,
    val isFavorite: Boolean = false
)

@Dao
interface StockDao {
    @Query("SELECT * FROM stocks")
    fun getAllStocks(): Flow<List<Stock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<Stock>)
}
```

### Dependency Injection (Hilt)
- Application: `@HiltAndroidApp`
- Activity: `@AndroidEntryPoint`
- Modules: `@Module` + `@InstallIn(SingletonComponent::class)`
- Example:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VettrDatabase {
        return Room.databaseBuilder(context, VettrDatabase::class.java, "vettr-db").build()
    }
}
```

### Design System
- NEVER hardcode colors or fonts
- Always use: `VettrColors.Navy`, `VettrTypography.Title`, `MaterialTheme.colorScheme`
- All design tokens defined in `designsystem/theme/`

### Networking (Retrofit)
- Define interfaces with suspend functions
- Use `@GET`, `@POST`, etc.
- OkHttp interceptors for auth headers
- Example:
```kotlin
interface VettrApi {
    @GET("stocks")
    suspend fun getStocks(): List<StockDto>
}
```

### Navigation
- Use Navigation Compose with type-safe routes
- Bottom navigation with 5 tabs: Pulse, Discovery, Stocks, Alerts, Profile
- Example:
```kotlin
sealed class Screen(val route: String) {
    object Pulse : Screen("pulse")
    object Discovery : Screen("discovery")
    // ...
}
```

---

## Common Gotchas

1. **Hilt:** Every `@AndroidEntryPoint` activity needs `@HiltAndroidApp` on Application class
2. **Room:** Entity classes must have a primary constructor with all fields as parameters
3. **Compose:** Remember to use `remember` and `rememberSaveable` for state preservation
4. **Coroutines:** Always use `Dispatchers.IO` for database/network operations
5. **StateFlow:** Use `collectAsStateWithLifecycle()` in Compose (NOT `collectAsState()`)
6. **Navigation:** Use `NavHost` inside a `Scaffold` with `BottomNavigation`
7. **Material 3:** Use `MaterialTheme.colorScheme` not `MaterialTheme.colors` (M2)
8. **Gradle:** Use `libs.versions.toml` for version catalog when available
9. **Proguard:** Room entities need `@Keep` or proguard rules
10. **Context:** Use `@ApplicationContext` in Hilt modules, not Activity context

---

## Progress Report Format

APPEND to scripts/ralph/progress.txt (never replace, always append):

```
## [Story ID]: [Story Title]
Status: ✅ COMPLETE
Date: [date]
Details:
- What was implemented
- Files changed
- **Learnings for future iterations:**
  - Patterns discovered
  - Gotchas encountered
  - Useful context
---
```

---

## Stop Condition

After completing a user story, check if ALL stories have `passes: true`.

If ALL stories are complete and passing, reply with:
```
<promise>COMPLETE</promise>
```

If there are still stories with `passes: false`, end your response normally (another iteration will pick up the next story).

---

## Important

- Work on ONE story per iteration
- Commit frequently with descriptive messages
- Keep builds green (assembleDebug must pass)
- Read the Codebase Patterns section in progress.txt before starting
- Use mock/seed data for realistic test data (same 25 Canadian stocks as iOS)
- The app is a DARK THEME app — navy background (#0D1B2A), green accent (#00C853)
