package com.asd.btsearch

import com.asd.btsearch.classes.Coordinate
import com.asd.btsearch.classes.EstimateLocation
import com.asd.btsearch.classes.Measurement
import org.junit.Test
import org.junit.Assert.*

class EstimateLocationTest {
    @Test
    fun signalDistanceIsCorrect() {
        // https://medium.com/beingcoders/convert-rssi-value-of-the-ble-bluetooth-low-energy-beacons-to-meters-63259f307283
        val dist = EstimateLocation.rssiToMeters(-80f)
        assertEquals( 3.548134f, dist)
    }

    @Test
    fun locationEstimateIsCorrect() {
        val location:Coordinate = EstimateLocation.estimateLocation(
            Measurement(Coordinate(0f,0f), -90f),
            Measurement(Coordinate(0f,10f), -80f)
        )

        assertTrue(location.x in 3.4f..3.5f)
        assertTrue(location.y in 10.6f..10.7f)
    }

    @Test
    fun distanceFormulaIsCorrect() {
        assertEquals(
            EstimateLocation.distanceFormula(Coordinate(0f, 0f), Coordinate(0f, 10f)),
            10.0f
        )
    }
}