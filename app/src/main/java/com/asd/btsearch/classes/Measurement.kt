package com.asd.btsearch.classes

data class Measurement(val coordinates: Coordinate, val signalStrength: Float) {
    val xCoord = coordinates.x
    val yCoord = coordinates.y
}