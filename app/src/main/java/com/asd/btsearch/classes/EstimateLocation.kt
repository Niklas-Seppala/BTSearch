package com.asd.btsearch.classes

import android.util.Log
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class EstimateLocation {

    companion object {
        /**
         * Employs trilateration to roughly estimate a location of a bluetooth device
         * based on two measurements containing the signal strength values and the locations they were measured at
         * @param measurementA the first measurement
         * @param measurementB the second measurement
         * @return the estimated coordinates
         */
        fun estimateLocation(measurementA: Measurement, measurementB: Measurement): Coordinate {
            // https://en.wikipedia.org/wiki/True-range_multilateration
            val bX = abs(measurementA.xCoord - measurementB.xCoord)
            val bY = abs(measurementA.yCoord - measurementB.yCoord)

            val abDistance = distanceFormula(
                Coordinate(measurementB.xCoord, measurementB.yCoord),
                Coordinate(measurementB.xCoord, measurementB.yCoord)
            )
            Log.d("MeasurementView", "abDistance $abDistance")

            val r1 = abs(measurementA.signalStrength)
            val r2 = abs(measurementB.signalStrength)
            Log.d("MeasurementView", "bX $bX bY $bY")
            Log.d("MeasurementView", "r1 $r1 r2 $r2")
            // we are going to use the teachings of the "Plug and chug" school of mathematics
            val unknownY = (
                    ((r1.pow(2) - r2.pow(2)) + abDistance.pow(2)) / (abDistance * 2)
                    )

            val unknownX = sqrt(
                r1.pow(2) - (unknownY).pow(2)
            )

            val locationEstimate = Coordinate(unknownX, unknownY)
            println("X: ${locationEstimate.x} Y:${locationEstimate.y}")
            println("${(r1.pow(2) - r2.pow(2))}")
            println("${(abDistance.pow(2) / (abDistance * 2))}")
            println("${abDistance.pow(2)}")
            println("${(abDistance * 2)}")
            println("abDistance: $abDistance")
            println("r1: $r1 r2: $r2")
            println("unknownY: $unknownY")
            println("unknownX: $unknownX")
            return locationEstimate
        }

        /**
         * Employs basic trigonometry to calculate the rough distance between two points
         *
         * @param coordinateA the first coordinate
         * @param coordinateB the second coordinate
         * @return the estimated distance
         */
        fun distanceFormula(coordinateA: Coordinate, coordinateB: Coordinate): Float {
            val ax = coordinateA.x
            val ay = coordinateA.y

            val bx = coordinateB.x
            val by = coordinateB.y
            println("ax: $ax ay: $ay bx: $bx by: $by")

            return sqrt(
                (ax - bx).pow(2) + (ay - by).pow(2)
            )
        }

        /**
         * Estimates the distance between the user and a bluetooth device based
         * on the signal strength
         *
         * @param signalStrength the signal strength in dBm
         */
        fun rssiToMeters(signalStrength: Float): Float {
            // https://medium.com/beingcoders/convert-rssi-value-of-the-ble-bluetooth-low-energy-beacons-to-meters-63259f307283
            val power: Float = -69f
            val N = 2 //constant

            return (10f.pow(((power - signalStrength) / (10 * N))))
        }
    }
}