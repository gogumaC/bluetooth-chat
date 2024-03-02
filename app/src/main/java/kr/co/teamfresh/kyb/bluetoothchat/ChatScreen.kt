package kr.co.teamfresh.kyb.bluetoothchat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatService
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