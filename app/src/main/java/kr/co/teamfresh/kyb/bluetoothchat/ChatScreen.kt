package kr.co.teamfresh.kyb.bluetoothchat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.BluetoothChatService
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme


@Composable
fun ChatScreen(
    onClickPlusButton: () -> Unit,
    modifier: Modifier = Modifier,
    service: BluetoothChatService? = null
) {

    var showPairedDeviceDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Box() {
            Column() {
                Button(
                    onClick = { showPairedDeviceDialog = true },
                    modifier = Modifier.size(width = 160.dp, height = 100.dp)
                ) {
                    Text(text = "Find device")
                }
                Button(onClick = { }, modifier = Modifier.size(width = 160.dp, height = 100.dp)) {
                    Text(text = "Paring list")
                }
                Button(onClick = { }, modifier = Modifier.size(width = 160.dp, height = 100.dp)) {
                    Text(text = "Send Hello")
                }
            }

            if (showPairedDeviceDialog) PairedDeviceDialog(
                onClickPlusButton = onClickPlusButton,
                onDismissRequest = { showPairedDeviceDialog = false },
                deviceList = service?.getPairedDeviceList()?.toList() ?: listOf()
            )
        }


    }


}



@Preview(showBackground = true, showSystemUi = false)
@Composable
fun ChatScreenPreview() {
    BluetoothChatTheme {
        ChatScreen({}, Modifier.fillMaxSize())
    }
}

@Preview
@Composable
fun PairedDeviceListDialogPreview() {
    BluetoothChatTheme {
        PairedDeviceDialog({}, onDismissRequest = { }, List(3) { "Device$it" })
    }
}

@Preview
@Composable
fun BluetoothDeviceInfoUnitPreview() {
    BluetoothChatTheme {
        BluetoothDeviceInfoUnit(deviceName = "device1")
    }
}