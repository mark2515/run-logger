package moe.kunlonghe.myruns.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MyRunsRepository(private val myRunsEntryDao: MyRunsEntryDao) {

    val allMyRunsEntries: Flow<List<MyRunsEntry>> = myRunsEntryDao.getAllMyRunsEntries()

    fun insert(entry: MyRunsEntry) {
        CoroutineScope(IO).launch {
            myRunsEntryDao.insertMyRunsEntry(entry)
        }
    }

    fun delete(id: Long) {
        CoroutineScope(IO).launch {
            myRunsEntryDao.deleteMyRunsEntry(id)
        }
    }

    fun deleteAll() {
        CoroutineScope(IO).launch {
            myRunsEntryDao.deleteAll()
        }
    }

    suspend fun getEntry(id: Long): MyRunsEntry? {
        return myRunsEntryDao.getMyRunsEntry(id)
    }
}

