package moe.kunlonghe.myruns.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseEntryDao {

    @Insert
    suspend fun insertExerciseEntry(entry: ExerciseEntry)

    @Query("SELECT * FROM exercise_table ORDER BY date_time DESC")
    fun getAllExerciseEntries(): Flow<List<ExerciseEntry>>

    @Query("SELECT * FROM exercise_table WHERE id = :key")
    suspend fun getExerciseEntry(key: Long): ExerciseEntry?

    @Query("DELETE FROM exercise_table")
    suspend fun deleteAll()

    @Query("DELETE FROM exercise_table WHERE id = :key")
    suspend fun deleteExerciseEntry(key: Long)

    @Update
    suspend fun updateExerciseEntry(entry: ExerciseEntry)
}

