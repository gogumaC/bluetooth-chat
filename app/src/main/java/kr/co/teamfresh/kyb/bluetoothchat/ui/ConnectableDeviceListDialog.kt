package kr.co.teamfresh.kyb.bluetoothchat.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.BluetoothState
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme

@SuppressLint("MissingPermission")
@Composable
fun ConnectableDeviceListDialog(
    deviceList: List<BluetoothDevice>,
    bluetoothDiscoveringState: BluetoothState,
    onSelectDevice: (BluetoothDevice) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    var selectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }
    val isFinding = (bluetoothDiscoveringState == BluetoothState.STATE_DISCOVERING)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.aspectRatio(0.7f),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("연결 가능한 기기 목록", modifier = Modifier.padding(16.dp))
                if (isFinding) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(16.dp)
                            .size(28.dp)
                    )
                }
            }
            HorizontalDivider(color = Color.LightGray)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp),
                contentPadding = PaddingValues(8.dp)
            ) {
                items(deviceList) {
                    BluetoothDeviceItem(
                        modifier = Modifier.clickable {
                            selectedDevice = it
                            onSelectDevice(it)
                        },
                        name = it.name,
                        macAddress = it.address,
                        borderColor = if (selectedDevice == it) Color.Blue else Color.LightGray,
                        borderWidth = if (selectedDevice == it) 1.dp else 0.4.dp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectableDeviceListDialogPreview() {
    BluetoothChatTheme {
        Surface {
            ConnectableDeviceListDialog(
                listOf(),
                BluetoothState.STATE_DISCOVERING,
                onSelectDevice = {}) {}
        }
    }
}