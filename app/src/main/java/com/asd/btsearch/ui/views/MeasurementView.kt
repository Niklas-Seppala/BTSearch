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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.asd.btsearch.classes.Measurement
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asd.btsearch.classes.Coordinate
import com.asd.btsearch.R
import com.asd.btsearch.classes.DeviceLocationEstimate
import com.asd.btsearch.classes.EstimateLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.intellij.lang.annotations.JdkConstants
import kotlin.math.abs

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
    val isMeasuring = MutableLiveData<Boolean>(false)
    val SCAN_PERIOD = 5000L

    val deviceIsCloser = MutableLiveData<Boolean>(false);


    private val deviceRssiHistoryLength = 5
    // list of the past signal strength values we've gotten from the device
    // used to determine if we are getting closer or going away from the device
    var deviceRssiHistory = MutableLiveData<List<Int>>(listOf())

    private val mResults = java.util.HashMap<String, ScanResult>()

    // suppress because we are going to ask them in MainActivity
    @SuppressLint("MissingPermission")
    fun scanDevices(scanner: BluetoothLeScanner) {
        viewModelScope.launch(Dispatchers.IO) {
            scanResults.postValue(listOf())
            isScanning.postValue(true)

            // store the pre-existing signal strength value for later comparison
            var oldRssi = chosenDevice.value?.rssi

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()
            scanner.startScan(null, settings, leScanCallback)
            delay(SCAN_PERIOD)
            scanner.stopScan(leScanCallback)
            scanResults.postValue(mResults.values.toList())
            isScanning.postValue(false)
            Log.d(TAG, "Scan in progress isMeasuring: ${isMeasuring.value} chosenDevice: ${chosenDevice.value}, measurement count = ${_measurements.value?.count()}]}")
            if(
                chosenDevice.value != null && (isMeasuring.value == true || _measurements.value?.count()?:0 == 2)
            ) {
                // add measurement here when we can be sure its updated
                var device = scanResults.value?.find { it.device.address == viewModel.chosenDevice.value?.device?.address }
                Log.d(TAG, "Device ${device?.device?.address} new RSSI is ${device?.rssi} ")
                var measurementsList = _measurements.value?.toMutableList()

                var xCoord = location.value?.latitude?.toFloat()?:0f
                var yCoord = location.value?.longitude?.toFloat()?:0f


                var coordinate = Coordinate(xCoord, yCoord)
                if(measurementsList?.count()?:0 < 2) {
                    measurementsList?.add(
                        Measurement(
                            coordinate, device?.rssi?.toFloat() ?: 0f
                        )
                    )
                    measurements.postValue(measurementsList)
                    Log.d(TAG, "Measurements are now ${measurements}")

                }
                if(measurementsList?.count() == 2) {
                    // re-trigger scan, as we are now in tracking mode
                    Log.d(TAG, "re-trigger scan")
                    scanDevices(scanner)
                }
                isMeasuring.postValue(false)

                var _deviceRssiHistory = deviceRssiHistory.value?.toMutableList()

                // remove the oldest signal strength value
                if(_deviceRssiHistory?.count()?:0 > deviceRssiHistoryLength) {
                    _deviceRssiHistory?.removeFirst()
                }
                _deviceRssiHistory?.add(device?.rssi ?: 0)

                deviceRssiHistory.postValue(_deviceRssiHistory?.toList())
                chosenDevice.postValue(device)

                // determine if we are getting closer or not by determining the change in signal strength
                // in signal strength
                if(deviceRssiHistory.value?.count()?:0 >= 2) {
                    val v1 = deviceRssiHistory.value?.first() ?: 0
                    val v2 = deviceRssiHistory.value?.last() ?: 0

                    val changePercentage = ((v2 - v1).toFloat() / v1.toFloat())*100f

                    Log.d(TAG,
                        "Old RSSI: ${oldRssi}, new RSSI ${device?.rssi} change % ${changePercentage}, closer = ${changePercentage < 0}")


                    // negative change
                    deviceIsCloser.postValue(
                        changePercentage < 0
                    )
                }
            }
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

    val chosenDevice = viewModel.chosenDevice.observeAsState()
    val isScanning = viewModel.isScanning.observeAsState(false)
    val deviceIsCloser = viewModel.deviceIsCloser.observeAsState(false)


    Box(Modifier.fillMaxSize()) {

        Column{
            if(chosenDevice.value != null){
                Text("Chosen device addr: ${chosenDevice.value?.device?.address}")
                Text("Chosen device rssi: ${chosenDevice.value?.rssi}")

                // TODO: own component, implement graphical meter
                if(deviceIsCloser.value) {Text("Getting closer")}
                else {Text("Going away")}
            }
            LazyColumn {
                items(measurements?.value ?: listOf()) {
                    it ->
                    Text("(${it.xCoord}, ${it.yCoord}, ${it.signalStrength})")
                }
            }
            MeasurementInstructions()
            Row{
                Button(
                    onClick = { addMeasurement() },
                    enabled = canMeasure()
                ) {
                    MeasureButton()
                }
                Button(onClick = {
                    if(chosenDevice.value != null) {
                        viewModel.chosenDevice.postValue(null)
                        viewModel.measurements.postValue(listOf())
                    }
                    viewModel.scanDevices(btAdapter.bluetoothLeScanner)

                }, enabled=(
                        !isScanning.value || chosenDevice.value != null
                    )
                ) {
                    if(chosenDevice.value == null ) { Text("Scan") }
                    else {
                        Text("Switch device")
                    }
                }
            }

            if(chosenDevice.value == null) { DeviceList() }

        }

    }
}

@Composable
fun MeasureButton() {
    val isMeasuring = viewModel.isMeasuring.observeAsState()
    var text = "Measure" // TODO: strings.xml
    if(isMeasuring.value == true) {
        text = "Measuring"
    }
    Text(text)
}

@Composable
fun DeviceList() {
    val isScanning = viewModel.isScanning.observeAsState(false)
    val scanResults: List<ScanResult> by viewModel.scanResults.observeAsState(listOf())

    if(!isScanning.value) {
        LazyColumn(Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)) {
            items(scanResults ?: listOf()) { it ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${it.scanRecord?.deviceName} ${it.device.address} rssi: ${it.rssi}")
                        Button(onClick = { viewModel.chosenDevice.postValue(it) }, ) { Text("Choose") }
                    }
                }
            }
        }
    } else {
        Text("SCANNING")
    }
}

@Composable
fun MeasurementInstructions() {
    var measurements = viewModel.measurements.observeAsState()
    var chosenDevice = viewModel.chosenDevice.observeAsState()

    // TODO: implement possibility for more than 2 messages, use an array
    if(chosenDevice.value != null) {
        if (measurements.value?.count() == 0) {
            Text(LocalContext.current.getString(R.string.measurement_view_instruction_2))
        } else if (measurements.value?.count() == 1) {
            Text(LocalContext.current.getString(R.string.measurement_view_instruction_3))
        } else if (measurements.value?.count()!! >= 2) {
            Column {
                Text("Estimated coordinates")
                Text("${EstimateLocation.estimateLocation(measurements?.value!!.get(0), measurements?.value!!.get(1)).toString()}")
            }

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

fun scanDevice() {
    /**
     * Scans for bluetooth devices nearby and fetches the RSSI value for the chosen device
     */
    // re-initiate a scan
    viewModel.isMeasuring.postValue(true)
    viewModel.scanDevices(btAdapter.bluetoothLeScanner)
    //var device = viewModel.chosenDevice.value
    // find the device we want via MAC address
}

fun canMeasure():Boolean {
    if(viewModel.chosenDevice.value == null) {
        return false
    }
    if(viewModel.isScanning.value == true) {
        return false
    }

    Log.d(TAG, "CanMeasure, measurement count ${viewModel.measurements.value} ${viewModel.measurements.value?.count()}")
    if(viewModel.measurements.value?.count()?:0 >= 2) {
        return false
    }
    return true
}

fun addMeasurement() {

    Log.d(TAG, "Adding, values now ${viewModel.measurements.value.toString()}, count ${viewModel.measurements.value?.count()}")
    var measurements = viewModel.measurements.value?.toMutableList()
    getLocation()

    var location = viewModel.location.value

    if(canMeasure()) {
        scanDevice() // scanDevice -> bluetoothScan Callback -> adds measurement
    }
    viewModel.measurements.postValue(measurements)
}