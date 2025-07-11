package com.example.dockerapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.dockerapp.data.model.UserCredentials
import com.example.dockerapp.data.model.GrafanaCredentials

@Database(entities = [UserCredentials::class, GrafanaCredentials::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userCredentialsDao(): UserCredentialsDao
    abstract fun grafanaCredentialsDao(): GrafanaCredentialsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Créer la nouvelle table grafana_credentials
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `grafana_credentials` (
                        `id` INTEGER NOT NULL,
                        `username` TEXT NOT NULL,
                        `password` TEXT NOT NULL,
                        `serverUrl` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "docker_app_database"
                )
                .addMigrations(MIGRATION_3_4)
                .fallbackToDestructiveMigration() // Pour gérer la migration de version
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
