# Room Database Migration Guide

This guide explains how to handle database schema changes in VETTR Android app.

## Overview

- **Current Version**: 1
- **Schema Location**: `app/schemas/`
- **Export Schema**: Enabled (generates JSON schema files for each version)

## When to Create a Migration

Create a migration when you:
- Add a new entity (table)
- Remove an entity
- Add/remove columns to existing entities
- Change column types
- Add/remove indexes or foreign keys

## Migration Strategy

### Development Builds
For debug builds, we use `fallbackToDestructiveMigration()`, which drops and recreates all tables when a migration is missing. This is acceptable during development since data is not production-critical.

### Production Builds
For release builds, ALL migrations must be explicitly defined to preserve user data.

## How to Add a Migration

### Step 1: Update Your Entity
Modify your entity class (e.g., `Stock.kt`, `Filing.kt`) with the new schema.

### Step 2: Increment Database Version
Update the version in `VettrDatabase.kt`:
```kotlin
@Database(
    entities = [...],
    version = 2, // increment this
    exportSchema = true
)
```

### Step 3: Create Migration Object
Create a migration in `Migrations.kt`:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: Add new column
        database.execSQL(
            "ALTER TABLE stocks ADD COLUMN newColumn TEXT DEFAULT '' NOT NULL"
        )
    }
}
```

### Step 4: Add Migration to DatabaseModule
Update `DatabaseModule.kt`:
```kotlin
Room.databaseBuilder(context, VettrDatabase::class.java, "vettr-db")
    .addMigrations(MIGRATION_1_2)
    .build()
```

### Step 5: Test Migration
1. Install app with old version
2. Add some test data
3. Update to new version
4. Verify data is preserved and new schema is applied

## Common Migration Examples

### Adding a Column
```kotlin
database.execSQL("ALTER TABLE stocks ADD COLUMN description TEXT DEFAULT '' NOT NULL")
```

### Creating a New Table
```kotlin
database.execSQL("""
    CREATE TABLE IF NOT EXISTS watchlists (
        id TEXT PRIMARY KEY NOT NULL,
        name TEXT NOT NULL,
        createdAt INTEGER NOT NULL
    )
""")
```

### Adding an Index
```kotlin
database.execSQL("CREATE INDEX IF NOT EXISTS index_stocks_ticker ON stocks(ticker)")
```

### Renaming a Column (SQLite doesn't support RENAME COLUMN before API 30)
```kotlin
// Create new table with correct schema
database.execSQL("CREATE TABLE stocks_new (...)")
// Copy data
database.execSQL("INSERT INTO stocks_new SELECT ... FROM stocks")
// Drop old table
database.execSQL("DROP TABLE stocks")
// Rename new table
database.execSQL("ALTER TABLE stocks_new RENAME TO stocks")
```

## Schema Export Location

Schema JSON files are exported to: `app/schemas/com.vettr.android.core.data.local.VettrDatabase/`

Each version creates a new JSON file (e.g., `1.json`, `2.json`). These should be committed to version control.

## Testing Migrations

Room provides testing support via `MigrationTestHelper`:
```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        VettrDatabase::class.java
    )

    @Test
    fun migrate1To2() {
        // Create database with version 1
        val db = helper.createDatabase(TEST_DB, 1)
        // Insert test data
        db.execSQL("INSERT INTO stocks VALUES (...)")
        db.close()

        // Run migration
        helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // Verify data
        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 2, true)
        // Assert data is preserved
    }
}
```

## Fallback Strategy

**Development Only**: If a migration is missing in debug builds, Room will drop all tables and recreate them. This is configured in `DatabaseModule.kt` with:
```kotlin
if (BuildConfig.DEBUG) {
    .fallbackToDestructiveMigration()
}
```

**Production**: Missing migrations will cause the app to crash. Always define migrations for production builds.

## Best Practices

1. **Always increment version** when changing schema
2. **Test migrations** with real data before releasing
3. **Keep migrations simple** - one logical change per migration
4. **Document breaking changes** in migration comments
5. **Never modify old migrations** - they may have already run on user devices
6. **Commit schema files** to Git for version history
7. **Use transactions** for complex multi-step migrations (Room does this automatically)

## Resources

- [Room Migration Documentation](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Testing Room Migrations](https://developer.android.com/training/data-storage/room/testing-db)
