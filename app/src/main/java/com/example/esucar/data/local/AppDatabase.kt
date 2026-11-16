package com.example.esucar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DescargaMaterial::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun descargaDao(): DescargaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "descargas_db"
                ).build().also { INSTANCE = it }
            }
    }
}