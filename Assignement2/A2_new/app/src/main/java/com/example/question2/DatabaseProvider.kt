package com.example.question2

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "flight_database"
            )
                .fallbackToDestructiveMigration()  // This will clear the DB if the schema has changed.
                .build()
            INSTANCE = instance
            instance
        }
    }
}
