package com.asd.btsearch.ui.views

import android.content.Context
import android.provider.Settings.Global.getString
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.asd.btsearch.classes.Coordinate
import com.asd.btsearch.R
import com.asd.btsearch.classes.EstimateLocation

private const val TAG = "MeasurementView"



class MeasurementViewModel: ViewModel() {
    private var _measurements = MutableLiveData<List<Measurement>>(mutableListOf())
    var measurements: MutableLiveData<List<Measurement>> = _measurements
}

lateinit var viewModel:MeasurementViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MeasurementView(navigation: NavHostController, vm: MeasurementViewModel = viewModel(), permissionsState: MultiplePermissionsState) {

    viewModel = vm
    var measurements = viewModel.measurements.observeAsState()

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
        Text("${EstimateLocation.estimateLocation(measurements?.value!!.get(0), measurements?.value!!.get(1)).toString()}")
    }
}

fun addMeasurement() {
    Log.d(TAG, "Adding, values now ${viewModel.measurements.value.toString()}, count ${viewModel.measurements.value?.count()}")
    var measurements = viewModel.measurements.value?.toMutableList()
    if(viewModel.measurements.value?.count() ?: 0 == 0) {
        measurements?.add(Measurement(Coordinate(0f,0f), -90f))
    } else if(viewModel.measurements.value?.count() == 1) {
        measurements?.add(Measurement(Coordinate(0f,10f), -80f))
    }

    viewModel.measurements.postValue(measurements)

}