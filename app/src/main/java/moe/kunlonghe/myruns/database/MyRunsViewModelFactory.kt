package moe.kunlonghe.myruns.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyRunsViewModelFactory(private val repository: MyRunsRepository) : 
    ViewModelProvider.Factory {
    
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyRunsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyRunsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

