package kr.co.teamfresh.kyb.bluetoothchat.ui

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.BluetoothChatService
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme


@Composable
fun ConnectScreen(
    modifier: Modifier = Modifier,
    service: BluetoothChatService? = null,
    onBluetoothDeviceScanRequest: () -> Unit,
) {





    var showDialog by remember { mutableStateOf(false)}
    Box {
        ConnectLayout(onBluetoothScanRequest = {
            showDialog=true
            onBluetoothDeviceScanRequest()
        })
        if(showDialog) ConnectableDeviceListDialog(deviceList = listOf(), onDismiss = {showDialog=false})
    }
}


@Composable
fun ConnectLayout(
    modifier: Modifier = Modifier,
    onBluetoothScanRequest: () -> Unit,
    service: BluetoothChatService? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("저장된 기기 목록")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(service?.getPairedDeviceList() ?: listOf()) {
                val name = it.name.toString()
                val address = it.address
                SwipeDeviceItem(
                    name = name,
                    macAddress = address,
                    requestConnectDevice = {},
                    requestDeleteDevice = {})//service?.connect(it.address)
            }
            item {
                TextButton(onClick = { onBluetoothScanRequest() }) {
                    Text(text = "+ 새 기기 연결하기", modifier = Modifier)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeDeviceItem(
    modifier: Modifier = Modifier,
    name: String,
    macAddress: String,
    requestDeleteDevice: () -> Unit,
    requestConnectDevice: () -> Unit
) {
    val swipeState =
        rememberSwipeToDismissBoxState(positionalThreshold = { it * 0.2f }, confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    requestDeleteDevice()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    requestConnectDevice()
                    false
                }

                else -> true
            }
        })
    val scale by animateFloatAsState(
        targetValue = if (swipeState.targetValue == SwipeToDismissBoxValue.Settled) 1f else 1.25f,
        label = ""
    )
    SwipeToDismissBox(modifier = modifier, state = swipeState, backgroundContent = {
        val color =
            if (swipeState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Color.Red else Color.Blue
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            color = color
        ) {
            val alignment =
                if (swipeState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            val icon =
                if (swipeState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Icons.Default.Delete else Icons.Default.MailOutline
            Box(
                contentAlignment = alignment, modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.scale(scale)
                )
            }

        }
    }) {
        BluetoothDeviceItem(name = name, macAddress = macAddress)
    }

}

@Composable
fun BluetoothDeviceItem(modifier: Modifier = Modifier, name: String, macAddress: String) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
            .border(width = 0.4.dp, color = Color.LightGray, shape = RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name)
            Text(text = macAddress)
        }
    }

}

@Composable
fun ConnectableDeviceListDialog(
    deviceList: List<BluetoothDevice>,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier=modifier.aspectRatio(0.7f)) {
            Text("연결 가능한 기기 목록",modifier=Modifier.padding(16.dp))
            HorizontalDivider()
            LazyColumn {
                items(deviceList) {
                    BluetoothDeviceItem(name = it.name, macAddress = it.address)
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
            ConnectableDeviceListDialog(listOf()) {}
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConnectScreenPreview() {
    BluetoothChatTheme {
        ConnectScreen(onBluetoothDeviceScanRequest = {})
    }
}

