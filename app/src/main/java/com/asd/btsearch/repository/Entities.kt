package com.asd.btsearch.repository

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class DeviceEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long,
    val name: String, val mac: String,
    val lat: Double, val lon: Double,
    val hasImg: Boolean = false,
    val isConnectable: Boolean) {

    companion object {
        val Example = DeviceEntity(
            id = 0,
            timestamp = 1665330260,
            name = "Samsung TV",
            mac = "00-B0-D0-63-C2-26",
            lat = 60.234282,
            lon = 24.834913,
//            hasImg = true,
            isConnectable = true
        )
    }
}