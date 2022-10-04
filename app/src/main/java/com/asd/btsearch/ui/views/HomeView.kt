package com.asd.btsearch.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.asd.btsearch.ui.theme.BTSearchTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

private const val TAG = "HomeView"

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeView(navigation: NavHostController, permissionsState: MultiplePermissionsState) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(8.dp)) {
        Card(modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(), elevation = 2.dp) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = "Home view", style = MaterialTheme.typography.h5)
                Row(modifier=Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround) {
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Hello")
                    }
                    Button(onClick = { /*TODO*/ }) {
                        Text(text = "Goodbye")
                    }
                }
            }
        }
    }
}

@Composable
fun ContentCard(modifier: Modifier = Modifier, content: @Composable ()->Unit) {
    Card(modifier = modifier.then(Modifier.padding(4.dp)), elevation = 2.dp) {
        Surface(modifier = Modifier.padding(8.dp)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
internal fun ContentCardPreview() {
    BTSearchTheme {
        Surface(modifier = Modifier.padding(8.dp)) {
            ContentCard {
                Text(text = "Hello")
            }
        }
    }
}
