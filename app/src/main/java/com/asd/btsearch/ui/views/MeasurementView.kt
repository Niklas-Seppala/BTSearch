package com.asd.btsearch.ui.views

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.provider.Settings.Global.getString
import android.renderscript.RenderScript
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.asd.btsearch.classes.Measurement
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asd.btsearch.classes.Coordinate
import com.asd.btsearch.R
import com.asd.btsearch.classes.EstimateLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource

import com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

private const val TAG = "MeasurementView"



class MeasurementViewModel: ViewModel() {
    private var _measurements = MutableLiveData<List<Measurement>>(mutableListOf())
    var measurements: MutableLiveData<List<Measurement>> = _measurements

    private var _location = MutableLiveData<Location>()
    var location: MutableLiveData<Location> = _location
}

lateinit var viewModel:MeasurementViewModel
lateinit var fusedLocationClient: FusedLocationProviderClient

lateinit var cxt: Context
lateinit var act: Activity

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MeasurementView(navigation: NavHostController, vm: MeasurementViewModel = viewModel(), permissionsState: MultiplePermissionsState) {

    viewModel = vm
    var measurements = viewModel.measurements.observeAsState()
    cxt = navigation.context
    act = LocalContext.current as Activity
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(cxt!!)

    Box(Modifier.fillMaxSize()) {

        Column{
            MeasurementInstructions()
            Button(onClick = { addMeasurement() }) {
                Text("Measure")
            }

            LazyColumn {
                items(measurements.value?:listOf()) {
                    it ->
                    Text("(${it.xCoord}, ${it.yCoord}) rssi: ${it.signalStrength}")
                }
            }
        }

    }
}

@Composable
fun MeasurementInstructions() {
    var measurements = viewModel.measurements.observeAsState()

    // TODO: implement possibility for more than 2 messages, use an array
    if(measurements.value?.count() == 0) {
        Text(LocalContext.current.getString(R.string.measurement_view_instruction_1))
    } else if(measurements.value?.count() == 1) {
        Text(LocalContext.current.getString(R.string.measurement_view_instruction_2))
    }
    else if(measurements.value?.count()!! >= 2) {
        //Text("${EstimateLocation.estimateLocation(measurements?.value!!.get(0), measurements?.value!!.get(1)).toString()}")
    }
}

fun getLocation() {
    /*
    var locationManager = act.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    if ((ContextCompat.checkSelfPermission(cxt, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
        ActivityCompat.requestPermissions(act, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2)
    }
    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, )
    */
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

fun addMeasurement() {
    Log.d(TAG, "Adding, values now ${viewModel.measurements.value.toString()}, count ${viewModel.measurements.value?.count()}")
    var measurements = viewModel.measurements.value?.toMutableList()
    getLocation()

    var location = viewModel.location.value
    /*if(viewModel.measurements.value?.count() ?: 0 == 0) {
        measurements?.add(Measurement(Coordinate(0f,0f), -90f))
    } else if(viewModel.measurements.value?.count() == 1) {
        measurements?.add(Measurement(Coordinate(0f,10f), -80f))
    }*/
    measurements?.add(Measurement(Coordinate(location?.latitude?.toFloat()?:0f,location?.longitude?.toFloat()?:0f), -80f))
    viewModel.measurements.postValue(measurements)
}