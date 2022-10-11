package com.asd.btsearch.classes

import android.bluetooth.BluetoothDevice

data class DeviceLocationEstimate(val device: BluetoothDevice, val coordinate:Coordinate) {}