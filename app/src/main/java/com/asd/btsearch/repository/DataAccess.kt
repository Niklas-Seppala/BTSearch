package com.asd.btsearch.repository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface DeviceDao {
    @Query("SELECT * FROM entries ORDER BY timestamp ASC")
    fun getEntries(): LiveData<List<DeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DeviceEntity): Long

    @Query("DELETE FROM entries")
    suspend fun deleteAllEntries(): Int

    @Query("DELETE FROM entries WHERE id == :id")
    suspend fun deleteEntry(id: Int) : Int
}