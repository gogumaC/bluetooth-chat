package com.gogumac.bluetoothchat.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gogumac.bluetoothchat.R
import com.gogumac.bluetoothchat.ui.theme.BluetoothChatTheme

@Composable
fun SelectConnectAcceptDialog(
    modifier: Modifier = Modifier,
    deviceName: String,
    deviceAddress: String,
    onConfirmed: () -> Unit,
    onCanceled: () -> Unit,
) {

    AlertDialog(
        modifier = modifier,
        onDismissRequest = onCanceled,
        confirmButton = {
            Button(onClick = onConfirmed) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onCanceled) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        text = {
            Column {

                Text(text = stringResource(id = R.string.device_name_colon, deviceName))
                Text(text = stringResource(id = R.string.device_address_colon, deviceAddress))
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = stringResource(id = R.string.connect_requested))
            }
        }
    )
}

@Preview
@Composable
fun SelectConnectAcceptDialogPreview() {
    Surface {
        BluetoothChatTheme {
            SelectConnectAcceptDialog(
                deviceName = "device1",
                deviceAddress = "111.11.111",
                onConfirmed = {},
                onCanceled = {})
        }
    }
}