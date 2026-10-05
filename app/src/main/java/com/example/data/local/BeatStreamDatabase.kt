package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DownloadedSongEntity::class], version = 1, exportSchema = false)
abstract class BeatStreamDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: BeatStreamDatabase? = null

        fun getDatabase(context: Context): BeatStreamDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BeatStreamDatabase::class.java,
                    "beatstream_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
