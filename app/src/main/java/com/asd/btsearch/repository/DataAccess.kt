package com.asd.btsearch.repository

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DeviceDao {
    @Query("SELECT * FROM entries ORDER BY timestamp DESC")
    fun getEntries(): LiveData<List<DeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DeviceEntity): Long

    @Query("DELETE FROM entries")
    suspend fun deleteAllEntries(): Int

    @Query("SELECT * FROM entries WHERE id == :id")
    suspend fun getDevice(id: Int): DeviceEntity?

    @Query("UPDATE entries SET hasImg = :toggle WHERE id == :id")
    suspend fun toggleImage(id: Int, toggle: Boolean)

    @Query("DELETE FROM entries WHERE id == :id")
    suspend fun deleteEntry(id: Int) : Int
}