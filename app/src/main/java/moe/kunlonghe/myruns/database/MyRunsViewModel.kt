package moe.kunlonghe.myruns.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData

class MyRunsViewModel(private val repository: MyRunsRepository) : ViewModel() {

    val allMyRunsEntriesLiveData: LiveData<List<MyRunsEntry>> = 
        repository.allMyRunsEntries.asLiveData()

    fun insert(entry: MyRunsEntry) {
        repository.insert(entry)
    }

    fun delete(id: Long) {
        repository.delete(id)
    }

    fun deleteAll() {
        repository.deleteAll()
    }
}

