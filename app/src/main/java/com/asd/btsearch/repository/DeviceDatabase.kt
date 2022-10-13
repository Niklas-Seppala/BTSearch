package com.asd.btsearch.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DeviceEntity::class], version = 2)
abstract class DeviceDatabase: RoomDatabase() {
    abstract fun deviceDao(): DeviceDao

    companion object {
        private var instance: DeviceDatabase? = null
        @Synchronized
        fun get(context: Context): DeviceDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeviceDatabase::class.java,
                    "device.db"
                ).build()
            }
            return instance!!
        }
    }
}