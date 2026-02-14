# Room Database Migration Testing

This directory contains migration tests for the SuledDatabase.

## Why Test Migrations?

- Ensure data integrity when upgrading the database
- Catch migration bugs before production
- Verify column types, indexes, and constraints
- Test data transformation logic

## Writing Migration Tests

### 1. Add Room Migration Testing Dependency

Already included in `build.gradle.kts`:
```kotlin
androidTestImplementation("androidx.room:room-testing:$roomVersion")
```

### 2. Create Migration Test

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val TEST_DB_NAME = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SuledDatabase::class.java
    )
    
    @Test
    fun migrate1To2() {
        // Create database with version 1
        helper.createDatabase(TEST_DB_NAME, 1).apply {
            // Insert test data in v1 format
            execSQL("INSERT INTO tournaments VALUES ('1', 'Tournament 1', ...)")
            close()
        }
        
        // Run migration
        helper.runMigrationsAndValidate(
            TEST_DB_NAME,
            2,
            true,
            DatabaseMigrations.MIGRATION_1_2
        )
        
        // Verify migrated data
        helper.openDatabase(TEST_DB_NAME, false).apply {
            // Query and verify data
            val cursor = query("SELECT * FROM tournaments WHERE id = '1'")
            assertTrue(cursor.moveToFirst())
            // Verify columns exist and data is correct
            close()
        }
    }
}
```

### 3. Run Migration Tests

```bash
./gradlew :app:connectedAndroidTest
```

## Schema Export

Room exports database schema to `app/schemas/` when `exportSchema = true`.

Generate schema:
```bash
./gradlew kaptDebugKotlin
```

Schema files are version controlled to track database changes over time.

## Migration Strategy

### During Development (v1)
- Use `fallbackToDestructiveMigration()` in DEBUG builds
- Allows rapid schema changes without writing migrations
- Data loss is acceptable during development

### Before Production Release
1. Set final schema for v1
2. Export schema: `./gradlew kaptDebugKotlin`
3. Commit schema JSON to version control
4. Remove `fallbackToDestructiveMigration()` from RELEASE builds

### After Production Release
**Never use `fallbackToDestructiveMigration()` in production!**

For every schema change:
1. Increment database version
2. Write Migration object
3. Add migration to DatabaseModule
4. Write migration test
5. Export new schema version
6. Commit schema JSON and migration code

## Best Practices

1. **Incremental Migrations**: Write migrations for each version increment
2. **Test Everything**: Test migrations with real-world data patterns
3. **Batch Updates**: Use transactions for multiple SQL statements
4. **Index Creation**: Add indexes after bulk inserts for performance
5. **Default Values**: Provide defaults when adding non-null columns
6. **Backup Data**: Document backup/restore procedures for critical data

## Common Migration Operations

### Add Column
```kotlin
database.execSQL("ALTER TABLE tournaments ADD COLUMN category TEXT")
```

### Add Column with Default
```kotlin
database.execSQL("ALTER TABLE tournaments ADD COLUMN priority INTEGER NOT NULL DEFAULT 0")
```

### Create Table
```kotlin
database.execSQL("""
    CREATE TABLE new_table (
        id TEXT PRIMARY KEY NOT NULL,
        name TEXT NOT NULL
    )
""")
```

### Create Index
```kotlin
database.execSQL("CREATE INDEX index_tournaments_status ON tournaments(status)")
```

### Rename Column (SQLite < 3.25.0)
```kotlin
// 1. Create new table with correct schema
// 2. Copy data from old table
// 3. Drop old table
// 4. Rename new table to old name
```

### Complex Transformation
```kotlin
// Use multiple SQL statements within migration
database.execSQL("BEGIN TRANSACTION")
try {
    database.execSQL("...")
    database.execSQL("...")
    database.execSQL("COMMIT")
} catch (e: Exception) {
    database.execSQL("ROLLBACK")
    throw e
}
```

## Reference

- [Room Migration Docs](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [MigrationTestHelper](https://developer.android.com/reference/androidx/room/testing/MigrationTestHelper)
