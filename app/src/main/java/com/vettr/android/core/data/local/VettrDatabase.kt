package com.vettr.android.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vettr.android.core.model.AlertRule
import com.vettr.android.core.model.Executive
import com.vettr.android.core.model.Filing
import com.vettr.android.core.model.RedFlagHistory
import com.vettr.android.core.model.Stock
import com.vettr.android.core.model.SyncHistory
import com.vettr.android.core.model.User
import com.vettr.android.core.model.VetrScoreHistory
import com.vettr.android.core.util.Converters

@Database(
    entities = [
        Stock::class,
        Filing::class,
        Executive::class,
        User::class,
        AlertRule::class,
        RedFlagHistory::class,
        VetrScoreHistory::class,
        SyncHistory::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class VettrDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun filingDao(): FilingDao
    abstract fun executiveDao(): ExecutiveDao
    abstract fun userDao(): UserDao
    abstract fun alertRuleDao(): AlertRuleDao
    abstract fun redFlagHistoryDao(): RedFlagHistoryDao
    abstract fun vetrScoreHistoryDao(): VetrScoreHistoryDao
    abstract fun syncHistoryDao(): SyncHistoryDao
}
