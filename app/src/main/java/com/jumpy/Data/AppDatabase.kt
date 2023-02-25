package com.jumpy.Data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Score::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scoreDao(): ScoreDao

    companion object {

        @Volatile
        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return instance ?: synchronized(this) {
                val i = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "highscore"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                instance = i
                // return instance
                instance as AppDatabase
            }
        }
    }
}