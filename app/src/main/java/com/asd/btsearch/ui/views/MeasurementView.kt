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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asd.btsearch.R
import com.asd.btsearch.classes.EstimateLocation
import com.asd.btsearch.classes.Measurement
import com.asd.btsearch.repository.DeviceDatabase
import com.asd.btsearch.repository.DeviceEntity
import com.asd.btsearch.ui.theme.Green200
import com.asd.btsearch.ui.theme.Red200
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.DecimalFormat

private const val TAG = "MeasurementView"
private const val MINIMUM_SAVE_DISTANCE = 1.0

class MeasurementViewModel : ViewModel() {
    private var _measurements = MutableLiveData<List<Measurement>>(mutableListOf())
    var measurements: MutableLiveData<List<Measurement>> = _measurements

    private var _chosenDevice = MutableLiveData<ScanResult>()
    var chosenDevice = _chosenDevice

    private var _scanResults = MutableLiveData<List<ScanResult>>(mutableListOf())
    var scanResults = _scanResults

    private var _isScanning = MutableLiveData(false)
    val isScanning = _isScanning

    private val scanPeriodSearch = 5000L
    private val scanPeriodTracing = 3000L

    val deviceDistance = MutableLiveData(0f)

    private val deviceRssiHistoryLength = 5

    // list of the past signal strength values we've gotten from the device
    // used to determine if we are getting closer or going away from the device
    private var deviceRssiHistory = MutableLiveData<List<Int>>(listOf())

    private val mResults = java.util.HashMap<String, ScanResult>()

    /**
     * Saves the Bluetooth device described by the [ScanResult] object to the local Room database
     *
     * @param device the [ScanResult] describing the chosen Bluetooth device
     */
    fun saveDevice(device: ScanResult, act: Activity, scaffoldState: ScaffoldState) {
        val db = DeviceDatabase.get(act.baseContext).deviceDao()
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(act.baseContext)
        getLocation(act, fusedLocationClient, onSuccessListener = {

            val saveMessage = act.baseContext.getString(R.string.measurement_view_device_saved)
            Log.d(TAG, "GOT LOCATION lat: ${it.latitude} long: ${it.longitude}")
            viewModelScope.launch(Dispatchers.IO) {
                db.insertEntry(
                    DeviceEntity(
                        timestamp = System.currentTimeMillis() / 1000,
                        name = device.scanRecord?.deviceName ?: "Unknown",
                        mac = device.device.address,
                        lat = it.latitude,
                        lon = it.longitude,
                        isConnectable = device.isConnectable
                    )
                )
                scaffoldState.snackbarHostState.showSnackbar(
                    saveMessage,
                    null,
                    SnackbarDuration.Long
                )
            }
        })
    }

    /**
     * Performs a Bluetooth scan and checks up on the Bluetooth device current being tracked if one is chosen
     *
     * @param scanner the [BluetoothLeScanner] object being used to perform the scan
     */
    // suppress because we are going to ask them in MainActivity
    @SuppressLint("MissingPermission")
    fun scanDevices(scanner: BluetoothLeScanner) {
        Log.d(TAG, "SCANDEVICES CALLED")

        viewModelScope.launch(Dispatchers.IO) {
            isScanning.postValue(true)

            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(0)
                .build()
            scanner.startScan(null, settings, leScanCallback)
            var period = scanPeriodSearch
            if (chosenDevice.value != null) {
                period = scanPeriodTracing
            }
            delay(period)
            scanner.stopScan(leScanCallback)

            scanResults.postValue(mResults.values.toList())
            Log.d(TAG, "Scanresults ${scanResults.value}")
            isScanning.postValue(false)
            Log.d(
                TAG,
                "Scan in progress chosenDevice: ${chosenDevice.value}, measurement count = ${_measurements.value?.count()}]}"
            )
            if (
                chosenDevice.value != null /*&& (isMeasuring.value == true || _measurements.value?.count()?:0 == 2)*/
            ) {
                // add measurement here when we can be sure its updated
                val device =
                    scanResults.value?.find { it.device.address == chosenDevice.value?.device?.address }

                val _deviceRssiHistory = deviceRssiHistory.value?.toMutableList()

                // remove the oldest signal strength value
                if ((_deviceRssiHistory?.count() ?: 0) > deviceRssiHistoryLength) {
                    _deviceRssiHistory?.removeFirst()
                }
                _deviceRssiHistory?.add(device?.rssi ?: 0)

                deviceRssiHistory.postValue(_deviceRssiHistory?.toList())
                chosenDevice.postValue(device)

                // determine if we are getting closer or not by determining the change in signal strength
                if ((deviceRssiHistory.value?.count() ?: 0) >= 2) {

                    val distance = EstimateLocation.rssiToMeters(device?.rssi?.toFloat() ?: 0f)

                    // estimate the distance over the average of the last few rssi values acquired
                    Log.d(TAG, "Updating devicedistance $distance")
                    deviceDistance.postValue(distance)
                }

                // re-trigger scan, as we are now in tracking mode
                Log.d(TAG, "re-trigger scan")
                scanDevices(scanner)
            }
        }
    }

    /**
     * The callback method for the Bluetooth scan
     */
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {

            super.onScanResult(callbackType, result)
            val device = result.device
            val deviceAddress = device.address
            mResults[deviceAddress] = result
            Log.d("MyApp", "Device address: $deviceAddress (${result.isConnectable})")
        }
    }
}


lateinit var btManager: BluetoothManager
lateinit var btAdapter: BluetoothAdapter

@Composable
fun MeasurementView(
    vm: MeasurementViewModel = viewModel(),
    scaffoldState: ScaffoldState
) {
    val act = LocalContext.current as Activity
    btManager = act.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    btAdapter = btManager.adapter

    Box(Modifier.fillMaxSize()) {
        val chosenDevice = vm.chosenDevice.observeAsState()
        val isScanning = vm.isScanning.observeAsState(false)
        val deviceDistance = vm.deviceDistance.observeAsState()

        val scanResults = vm.scanResults.observeAsState(listOf())
        Log.d(TAG, "ChosenDevice ${chosenDevice.value}")
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            if (chosenDevice.value != null) {
                Spacer(Modifier.fillMaxWidth(0.10f))
                DeviceInfo(chosenDevice.value!!, act, vm, scaffoldState)
                DistanceIndicator(chosenDevice.value!!, deviceDistance.value ?: 0f)
            }

            Button(
                modifier = Modifier.padding(top = 8.dp),
                onClick = {
                    if (chosenDevice.value != null) {
                        vm.chosenDevice.postValue(null)
                        vm.measurements.postValue(listOf())
                    }
                    vm.scanDevices(btAdapter.bluetoothLeScanner)
                }, enabled = (!isScanning.value || chosenDevice.value != null)
            ) {
                if (chosenDevice.value == null) {
                    Text(LocalContext.current.getString(R.string.measurement_view_scan))
                } else {
                    Text(LocalContext.current.getString(R.string.measurement_view_switch_device))
                }
            }

            if (chosenDevice.value == null) {
                Log.d(TAG, "Showing deviceList with ${scanResults.value ?: 0} results")

                if (!isScanning.value) {
                    DeviceList(scanResults.value ?: listOf(), onChooseDevice = {
                        vm.chosenDevice.postValue(it)
                        vm.scanDevices(btAdapter.bluetoothLeScanner)
                    })
                } else {
                    Column(modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(modifier = Modifier.padding(16.dp)) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .height(32.dp)
                                    .fillMaxWidth(),
                                color = Green200
                            )
                        }
                    }
                }
            } else {
                Text(LocalContext.current.getString(R.string.measurement_view_distance_instruction))
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceInfo(
    device: ScanResult,
    act: Activity,
    vm: MeasurementViewModel,
    scaffoldState: ScaffoldState
) {
    val distance = vm.deviceDistance.observeAsState()
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        DeviceCard(
            device = null,
            deviceName = device.scanRecord?.deviceName ?: "Unknown",
            deviceMac = device.device.address,
            deviceTimestamp = null,
            deviceLat = null,
            deviceLon = null,
        ) {}
        if (distance.value!! <= MINIMUM_SAVE_DISTANCE) {
            Log.d(TAG, "DeviceDistance ${distance.value}")
            Button(onClick = {
                vm.saveDevice(device, act, scaffoldState)
            }) {
                Text("Save this device")
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun DistanceIndicator(device: ScanResult, distance: Float) {
    val size = 200.dp
    var bgColor = Red200
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.DOWN

    if (distance <= MINIMUM_SAVE_DISTANCE) {
        bgColor = Green200
    }

    var showRssi by remember { mutableStateOf(false) }
    var content = "~${df.format(distance)}m"
    if (showRssi) {
        content = "${device.rssi} dBm"
    }

    Log.d(TAG, "Estimated distance $distance")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor)
                .clickable { showRssi = !showRssi }
        ) {
            Text(
                content, textAlign = TextAlign.Center,
                fontSize = 30.sp,
                modifier = Modifier
                    .width(size)
                    .height(size)
                    .wrapContentHeight()
            )
        }
    }
}

@Composable
fun DeviceList(scanResults: List<ScanResult>, onChooseDevice: (device: ScanResult) -> Unit) {
    LazyColumn(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .padding(horizontal = 5.dp)
    ) {
        items(scanResults.sortedBy { -it.rssi }) {
            CompactDeviceCard(it, onChooseDevice)
            Spacer(Modifier.padding(2.dp))
        }
    }
}

@Composable
fun CompactDeviceCard(device: ScanResult, onChooseDevice: (device: ScanResult) -> Unit = {}) {
    val df = DecimalFormat("#.##")
    df.roundingMode = RoundingMode.DOWN
    val distance = df.format(EstimateLocation.rssiToMeters(device.rssi.toFloat()))

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(5.dp)
        ) {
            Column(Modifier.fillMaxWidth(0.70f)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(LocalContext.current.getString(R.string.measurement_view_device_name))
                    Text(device.scanRecord?.deviceName ?: "Unknown")
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(LocalContext.current.getString(R.string.measurement_view_device_address))
                    Text(device.device.address)
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(LocalContext.current.getString(R.string.measurement_view_signal_strength))
                    Text(device.rssi.toString())
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(LocalContext.current.getString(R.string.measurement_view_distance_estimate))
                    Text("${distance}m")
                }
            }
            Spacer(Modifier.fillMaxWidth(0.05f))
            Button(onClick = { onChooseDevice(device) }, Modifier.fillMaxWidth()) {
                Text(LocalContext.current.getString(R.string.measurement_view_choose))
            }
        }
    }
}

/**
 * Pings for the current location of the user's device
 */
fun getLocation(
    act: Activity,
    locationClient: FusedLocationProviderClient,
    onSuccessListener: (location: Location) -> Unit
) {
    if (ActivityCompat.checkSelfPermission(
            act,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            act.baseContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return
    }
    @Suppress("DEPRECATION")
    locationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
        .addOnSuccessListener(onSuccessListener)
}

