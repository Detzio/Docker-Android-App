package com.example.dockerapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dockerapp.data.model.UserCredentials

@Database(entities = [UserCredentials::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userCredentialsDao(): UserCredentialsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "docker_app_database"
                )
                .fallbackToDestructiveMigration() // Pour gérer la migration de version
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
