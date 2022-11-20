package com.pixellore.checklist.DatabaseUtility

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TaskDatabase: RoomDatabase(){

    abstract fun actionItemDao(): TaskDao

    companion object{
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TaskDatabase{
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "action_plan_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(ActionListDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }


    private class  ActionListDatabaseCallback(private val scope: CoroutineScope)
        : RoomDatabase.Callback(){

        /**
         * Override the onCreate method to populate the database.
         */
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

        }

    }
}