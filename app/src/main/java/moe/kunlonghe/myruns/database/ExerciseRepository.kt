package moe.kunlonghe.myruns.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ExerciseRepository(private val exerciseEntryDao: ExerciseEntryDao) {

    val allExerciseEntries: Flow<List<ExerciseEntry>> = exerciseEntryDao.getAllExerciseEntries()

    fun insert(entry: ExerciseEntry) {
        CoroutineScope(IO).launch {
            exerciseEntryDao.insertExerciseEntry(entry)
        }
    }

    fun delete(id: Long) {
        CoroutineScope(IO).launch {
            exerciseEntryDao.deleteExerciseEntry(id)
        }
    }

    fun deleteAll() {
        CoroutineScope(IO).launch {
            exerciseEntryDao.deleteAll()
        }
    }

    suspend fun getEntry(id: Long): ExerciseEntry? {
        return exerciseEntryDao.getExerciseEntry(id)
    }
}

