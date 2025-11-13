package moe.kunlonghe.myruns.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MyRunsEntry::class], version = 3, exportSchema = false)
abstract class MyRunsDatabase : RoomDatabase() {
    
    abstract val myRunsEntryDao: MyRunsEntryDao

    companion object {
        @Volatile
        private var INSTANCE: MyRunsDatabase? = null

        fun getInstance(context: Context): MyRunsDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MyRunsDatabase::class.java,
                        "myruns_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}

