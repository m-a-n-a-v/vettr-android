package com.vettr.android.core.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vettr.android.core.model.Executive
import kotlinx.coroutines.flow.Flow

@Dao
interface ExecutiveDao {
    @Query("SELECT * FROM executives WHERE stock_id = :stockId")
    fun getByStockId(stockId: String): Flow<List<Executive>>

    @Query("SELECT * FROM executives WHERE name LIKE '%' || :query || '%'")
    fun searchByName(query: String): Flow<List<Executive>>

    @Query("SELECT * FROM executives WHERE id = :id")
    fun getById(id: String): Flow<Executive?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(executive: Executive)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(executives: List<Executive>)

    @Delete
    suspend fun delete(executive: Executive)
}
