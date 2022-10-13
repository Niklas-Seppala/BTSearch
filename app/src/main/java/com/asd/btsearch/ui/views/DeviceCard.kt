package com.asd.btsearch.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asd.btsearch.R
import com.asd.btsearch.repository.DeviceEntity
import com.asd.btsearch.ui.theme.BTSearchTheme
import com.asd.btsearch.ui.theme.Orange500
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable
fun DeviceCard(
    modifier: Modifier = Modifier,
    device: DeviceEntity?,
    deviceName: String?,
    deviceMac: String,
    deviceTimestamp: Long?,
    deviceLat: Double?,
    deviceLon: Double?,
    onJumpToLocation: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .then(modifier),
        elevation = 6.dp, backgroundColor = Orange500
    ) {
        Card(
            elevation = 4.dp,
            shape = MaterialTheme.shapes.medium.copy(
                bottomStart = ZeroCornerSize,
                topStart = ZeroCornerSize,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
        ) {
            Column(Modifier.padding(8.dp)) {
                Row {
                    Text(text = deviceName?:"Unknown", style = MaterialTheme.typography.h4)
                    Spacer(modifier = Modifier.weight(1.0f))

                    if(device != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, "Delete this device")
                        }
                    }
                }
                Column(modifier = Modifier.padding(4.dp)) {
                    Text(text = deviceMac)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Connectable")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.Check, "", modifier = Modifier.height(18.dp))
                    }
                    if(deviceLat != null && deviceLon != null) {
                        Location(device = device,
                            deviceLat = deviceLat,
                            deviceLon = deviceLon,
                            onClick = onJumpToLocation)
                    }
                }
                if(deviceTimestamp != null) {
                    Text(
                        modifier = Modifier.align(Alignment.End),
                        text = DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(deviceTimestamp)),
                        color = Color(0x88000000),
                        fontStyle = FontStyle.Italic,
                        fontSize = 13.sp
                    )
                }

            }
        }
    }
}

@Composable
private fun Location(
    modifier: Modifier = Modifier,
    device: DeviceEntity?,
    deviceLat: Double?,
    deviceLon: Double?,
    onClick: () -> Unit
) {
    Surface(modifier = modifier) {
        Button(onClick = onClick) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_location_24),
                    contentDescription = ""
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "${deviceLat}, ${deviceLon}")
            }
        }
    }
}

@Preview
@Composable
private fun DeviceCardPreview() {
    BTSearchTheme {
        Box(Modifier.padding(18.dp)) {
            DeviceCard(
                device = DeviceEntity.Example,
                deviceName = DeviceEntity.Example.name,
                deviceMac = DeviceEntity.Example.mac,
                deviceTimestamp = DeviceEntity.Example.timestamp,
                deviceLat = DeviceEntity.Example.lat,
                deviceLon = DeviceEntity.Example.lon,
                onDelete = {},
                onJumpToLocation = {}
            )
        }
    }
}
