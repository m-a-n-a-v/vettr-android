package com.vettr.android.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database migrations.
 *
 * IMPORTANT:
 * - Each migration must handle schema changes between consecutive versions
 * - Never modify existing migrations - they may have already run on user devices
 * - Test migrations thoroughly before releasing to production
 * - See MIGRATION_GUIDE.md for detailed instructions
 */

/**
 * Migration from version 1 to version 2.
 *
 * Template for future migrations. Replace this with actual schema changes when needed.
 *
 * Example usage:
 * ```
 * // In DatabaseModule.kt:
 * Room.databaseBuilder(context, VettrDatabase::class.java, "vettr-db")
 *     .addMigrations(MIGRATION_1_2)
 *     .build()
 * ```
 */
@Suppress("unused")
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // TODO: Replace with actual schema changes when migrating from version 1 to 2
        //
        // Examples:
        //
        // Add a column:
        // database.execSQL("ALTER TABLE stocks ADD COLUMN description TEXT DEFAULT '' NOT NULL")
        //
        // Create a new table:
        // database.execSQL("""
        //     CREATE TABLE IF NOT EXISTS watchlists (
        //         id TEXT PRIMARY KEY NOT NULL,
        //         name TEXT NOT NULL,
        //         createdAt INTEGER NOT NULL
        //     )
        // """)
        //
        // Add an index:
        // database.execSQL("CREATE INDEX IF NOT EXISTS index_stocks_ticker ON stocks(ticker)")
    }
}

/**
 * Add additional migrations as needed:
 *
 * val MIGRATION_2_3 = object : Migration(2, 3) {
 *     override fun migrate(database: SupportSQLiteDatabase) {
 *         // Schema changes for version 3
 *     }
 * }
 *
 * val MIGRATION_3_4 = object : Migration(3, 4) {
 *     override fun migrate(database: SupportSQLiteDatabase) {
 *         // Schema changes for version 4
 *     }
 * }
 */
