package com.asd.btsearch.classes

import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt

class EstimateLocation {

    companion object {
        fun estimateLocation(measurementA: Measurement, measurementB: Measurement): Coordinate {
            // https://en.wikipedia.org/wiki/True-range_multilateration
            val abDistance = distanceFormula(measurementA.coordinates, measurementB.coordinates)

            val r1 = rssiToMeters(measurementA.signalStrength)
            val r2 = rssiToMeters(measurementB.signalStrength)

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
            println("abDistance: ${abDistance}")
            println("r1: ${r1} r2: ${r2}")
            println("unknownY: ${unknownY}")
            println("unknownX: ${unknownX}")
            return locationEstimate
        }

        fun distanceFormula(coordinateA: Coordinate, coordinateB: Coordinate): Float {
            val ax = coordinateA.x
            val ay = coordinateA.y

            val bx = coordinateB.x
            val by = coordinateB.y
            println("ax: $ax ay: $ay bx: $bx by: $by")

            val dist = sqrt(
                (ax - bx).pow(2) + (ay - by).pow(2)
            )

            return dist
        }

        fun rssiToMeters(signalStrength: Float): Float {
            // TODO: unit test
            // https://medium.com/beingcoders/convert-rssi-value-of-the-ble-bluetooth-low-energy-beacons-to-meters-63259f307283
            val power: Float = -69f // TODO: Can we get this somehow?
            val N = 2 //constant, TODO: check if this needs/should be tweakable

            // ((power-signalStrength)/(10*N) )
            return (10f.pow(((power - signalStrength) / (10 * N))))
        }
    }
}