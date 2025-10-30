package moe.kunlonghe.myruns.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

class ExerciseViewModel(private val repository: ExerciseRepository) : ViewModel() {

    val allExerciseEntriesLiveData: LiveData<List<ExerciseEntry>> = 
        repository.allExerciseEntries.asLiveData()

    fun insert(entry: ExerciseEntry) {
        repository.insert(entry)
    }

    fun delete(id: Long) {
        repository.delete(id)
    }

    fun deleteAll() {
        repository.deleteAll()
    }
}

