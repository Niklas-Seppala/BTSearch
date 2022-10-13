package com.asd.btsearch.ui.views

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.asd.btsearch.R
import com.asd.btsearch.ui.theme.BTSearchTheme

@Composable
fun AppTitle(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_bluetooth_24),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = stringResource(id = R.string.app_name))
    }
}

@Preview(showBackground = true)
@Composable
internal fun AppTitlePreview() {
    BTSearchTheme {
        AppTitle()
    }
}