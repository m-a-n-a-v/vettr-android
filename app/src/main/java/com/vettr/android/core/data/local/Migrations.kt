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
 * Adds portfolio management tables:
 * - portfolios: User investment portfolios
 * - portfolio_holdings: Stock positions within portfolios
 * - portfolio_alerts: Auto-generated portfolio alerts
 * - portfolio_insights: AI-generated portfolio insights
 */
/**
 * Migration from version 2 to version 3.
 *
 * Adds enriched fields to portfolio_holdings:
 * - vetr_score: VETR quality score joined from stocks table
 * - price_change_percent: Price change percentage joined from stocks table
 * - name: Company name joined from stocks table
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE portfolio_holdings ADD COLUMN vetr_score INTEGER DEFAULT NULL")
        database.execSQL("ALTER TABLE portfolio_holdings ADD COLUMN price_change_percent REAL DEFAULT NULL")
        database.execSQL("ALTER TABLE portfolio_holdings ADD COLUMN name TEXT DEFAULT NULL")
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create portfolios table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS portfolios (
                id TEXT PRIMARY KEY NOT NULL,
                user_id TEXT NOT NULL,
                provider TEXT NOT NULL,
                account_id TEXT,
                name TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'active',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """)

        // Create portfolio_holdings table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS portfolio_holdings (
                id TEXT PRIMARY KEY NOT NULL,
                portfolio_id TEXT NOT NULL,
                ticker TEXT NOT NULL,
                quantity REAL NOT NULL,
                avg_cost REAL NOT NULL,
                current_price REAL NOT NULL DEFAULT 0.0,
                current_value REAL NOT NULL DEFAULT 0.0,
                gain_loss REAL NOT NULL DEFAULT 0.0,
                gain_loss_percent REAL NOT NULL DEFAULT 0.0,
                FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_portfolio_holdings_portfolio_id ON portfolio_holdings(portfolio_id)")

        // Create portfolio_alerts table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS portfolio_alerts (
                id TEXT PRIMARY KEY NOT NULL,
                portfolio_id TEXT NOT NULL,
                alert_type TEXT NOT NULL,
                severity TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                is_read INTEGER NOT NULL DEFAULT 0,
                triggered_at INTEGER NOT NULL,
                FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_portfolio_alerts_portfolio_id ON portfolio_alerts(portfolio_id)")

        // Create portfolio_insights table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS portfolio_insights (
                id TEXT PRIMARY KEY NOT NULL,
                portfolio_id TEXT NOT NULL,
                insight_type TEXT NOT NULL,
                severity TEXT NOT NULL,
                title TEXT NOT NULL,
                summary TEXT NOT NULL,
                is_dismissed INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL,
                FOREIGN KEY (portfolio_id) REFERENCES portfolios(id) ON DELETE CASCADE
            )
        """)
        database.execSQL("CREATE INDEX IF NOT EXISTS index_portfolio_insights_portfolio_id ON portfolio_insights(portfolio_id)")
    }
}
