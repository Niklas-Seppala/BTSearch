package com.asd.btsearch.ui.views

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.asd.btsearch.classes.Measurement
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asd.btsearch.classes.Coordinate
import com.asd.btsearch.R
import com.asd.btsearch.classes.DeviceLocationEstimate
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "MeasurementView"



class MeasurementViewModel: ViewModel() {
    private var _measurements = MutableLiveData<List<Measurement>>(mutableListOf())
    var measurements: MutableLiveData<List<Measurement>> = _measurements

    private var _location = MutableLiveData<Location>()
    var location: MutableLiveData<Location> = _location

    private var _locationEstimates = MutableLiveData<DeviceLocationEstimate>()
    var locationEstimates = _locationEstimates

    private var _chosenDevice = MutableLiveData<ScanResult>()
    var chosenDevice = _chosenDevice

    val scanResults = MutableLiveData<List<ScanResult>>(null)
    val isScanning = MutableLiveData<Boolean>(false)
    val SCAN_PERIOD = 5000L


    private val mResults = java.util.HashMap<String, ScanResult>()

    // suppress because we are going to ask them in MainActivity
    @SuppressLint("MissingPermission")
    fun scanDevices(scanner: BluetoothLeScanner) {
        viewModelScope.launch(Dispatchers.IO) {
            scanResults.postValue(listOf())
            isScanning.postValue(true)
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()
            scanner.startScan(null, settings, leScanCallback)
            delay(SCAN_PERIOD)
            scanner.stopScan(leScanCallback)
            scanResults.postValue(mResults.values.toList())
            isScanning.postValue(false)
        }
    }

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val deviceAddress = device.address
            mResults!![deviceAddress] = result
            Log.d("MyApp", "Device address: $deviceAddress (${result.isConnectable})")
        }
    }

}

lateinit var viewModel:MeasurementViewModel
lateinit var fusedLocationClient: FusedLocationProviderClient

lateinit var cxt: Context
lateinit var act: Activity
lateinit var btManager: BluetoothManager
lateinit var btAdapter: BluetoothAdapter

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MeasurementView(navigation: NavHostController, vm: MeasurementViewModel = viewModel(), permissionsState: MultiplePermissionsState) {

    viewModel = vm

    cxt = navigation.context
    act = LocalContext.current as Activity
    btManager = act.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    btAdapter = btManager.adapter

    fusedLocationClient = LocationServices.getFusedLocationProviderClient(cxt!!)

    var measurements = viewModel.measurements.observeAsState()
    val scanResults: List<ScanResult> by viewModel.scanResults.observeAsState(listOf())
    val chosenDevice = viewModel.chosenDevice.observeAsState()
    val isScanning = viewModel.isScanning.observeAsState(false)


    Box(Modifier.fillMaxSize()) {

        Column{
            Text("Chosen device addr: ${chosenDevice.value?.device?.address}")
            MeasurementInstructions()
            Row{
                Button(
                    onClick = { addMeasurement() },
                    enabled = canMeasure()
                ) {
                    Text("Measure")
                }
                Button(onClick = {
                    viewModel.scanDevices(btAdapter.bluetoothLeScanner)
                }, enabled=!isScanning.value) {
                    Text("Scan")
                }
            }

            LazyColumn {
                items(measurements.value?:listOf()) {
                    it ->
                    //Text("(${it.xCoord}, ${it.yCoord}) rssi: ${it.signalStrength}")
                }
            }
            if(!isScanning.value) {
                LazyColumn {
                    items(scanResults ?: listOf()) { it ->
                        Row{
                            Text("(${it.device.address}) rssi: ${it.rssi}")
                            Button(onClick = { viewModel.chosenDevice.postValue(it) }) {Text("X")}
                        }
                    }
                }
            } else {
                Text("SCANNING")
            }
        }

    }
}

@Composable
fun MeasurementInstructions() {
    var measurements = viewModel.measurements.observeAsState()

    // TODO: implement possibility for more than 2 messages, use an array
    if(viewModel.chosenDevice != null) {
        if (measurements.value?.count() == 0) {
            Text(LocalContext.current.getString(R.string.measurement_view_instruction_2))
        } else if (measurements.value?.count() == 1) {
            Text(LocalContext.current.getString(R.string.measurement_view_instruction_3))
        } else if (measurements.value?.count()!! >= 2) {
            //Text("${EstimateLocation.estimateLocation(measurements?.value!!.get(0), measurements?.value!!.get(1)).toString()}")
        }
    }
    else {
        Text(LocalContext.current.getString(R.string.measurement_view_instruction_1))
    }
}

fun getLocation() {

    if (ActivityCompat.checkSelfPermission(act,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            cxt,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    ) {

        return
    }
    fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token).addOnSuccessListener {
        location ->
        viewModel.location.postValue(location)
        Log.d(TAG, "lat: ${location.latitude} long: ${location.longitude}")
    }
}

fun estimateLocations() {
    var measurements = viewModel.measurements
    var scanResults = viewModel.scanResults

    if(measurements.value?.count() ?: 0 < 2) {

    }
}

fun scanDevice(scanResult: ScanResult?) {
    // re-initiate a scan
    viewModel.scanDevices(btAdapter.bluetoothLeScanner)
    //var device = viewModel.chosenDevice.value
    // find the device we want via MAC address
    var device = viewModel.scanResults.value?.find { it.device.address == viewModel.chosenDevice.value?.device?.address }
    Log.d(TAG, "Device ${device?.device?.address} new RSSI is ${device?.rssi} ")
}

fun canMeasure():Boolean {
    if(viewModel.chosenDevice.value == null) {
        return false
    }
    if(viewModel.isScanning.value == true) {
        return false
    }
    return true
}

fun addMeasurement() {
    var scanResult = viewModel.chosenDevice.value
    Log.d(TAG, "Adding, values now ${viewModel.measurements.value.toString()}, count ${viewModel.measurements.value?.count()}")
    var measurements = viewModel.measurements.value?.toMutableList()
    getLocation()

    var location = viewModel.location.value

    if(canMeasure()) {
        scanDevice(scanResult)

        measurements?.add(
            Measurement(
                Coordinate(location?.latitude?.toFloat() ?: 0f,
                    location?.longitude?.toFloat() ?: 0f), scanResult?.rssi?.toFloat() ?: 0f
            )
        )
    }
    viewModel.measurements.postValue(measurements)
}