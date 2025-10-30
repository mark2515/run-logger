package moe.kunlonghe.myruns.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MyRunsEntryDao {

    @Insert
    suspend fun insertMyRunsEntry(entry: MyRunsEntry)

    @Query("SELECT * FROM myruns_table ORDER BY date_time DESC")
    fun getAllMyRunsEntries(): Flow<List<MyRunsEntry>>

    @Query("SELECT * FROM myruns_table WHERE id = :key")
    suspend fun getMyRunsEntry(key: Long): MyRunsEntry?

    @Query("DELETE FROM myruns_table")
    suspend fun deleteAll()

    @Query("DELETE FROM myruns_table WHERE id = :key")
    suspend fun deleteMyRunsEntry(key: Long)

    @Update
    suspend fun updateMyRunsEntry(entry: MyRunsEntry)
}

