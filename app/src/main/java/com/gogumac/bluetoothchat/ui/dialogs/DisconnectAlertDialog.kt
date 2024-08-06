package com.gogumac.bluetoothchat.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gogumac.bluetoothchat.R

@Composable
fun DisconnectAlertDialog(
    onConfirmed: () -> Unit,
    onCanceled: () -> Unit,
    modifier: Modifier = Modifier
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
            Text(text = stringResource(id = R.string.disconnect_alert))
        })


}