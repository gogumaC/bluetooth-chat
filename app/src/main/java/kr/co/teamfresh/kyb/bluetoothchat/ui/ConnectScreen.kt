package kr.co.teamfresh.kyb.bluetoothchat.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kr.co.teamfresh.kyb.bluetoothchat.R
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.BluetoothService
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.BluetoothState
import kr.co.teamfresh.kyb.bluetoothchat.findActivity
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme


@Composable
fun ConnectScreen(
    modifier: Modifier = Modifier,
    service: BluetoothService? = null,
    onBluetoothDeviceScanRequest: () -> Unit,
    onDeviceConnected: () -> Unit,
    onChatScreenNavigateRequested: () -> Unit
) {

    Box(modifier = modifier) {
        ConnectLayout(
            service = service,
            onChatScreenButtonClicked = onChatScreenNavigateRequested,
            onBluetoothScanRequest = onBluetoothDeviceScanRequest)

    }
}

@SuppressLint("MissingPermission")
@Composable
fun ConnectLayout(
    modifier: Modifier = Modifier,
    onBluetoothScanRequest: () -> Unit,
    onChatScreenButtonClicked:()->Unit,
    service: BluetoothService? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier=modifier.weight(1f)) {
            Text("저장된 기기 목록")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(service?.getPairedDeviceList() ?: listOf()) {
                    val name = it.name
                    val address = it.address
                    SwipeDeviceItem(
                        name = name,
                        macAddress = address,
                        requestConnectDevice = { service?.connect(address) },
                        requestDeleteDevice = {})
                }
                item {
                    TextButton(onClick = { onBluetoothScanRequest() }) {
                        Text(text = "+ 새 기기 연결하기", modifier = Modifier)
                    }
                }
            }
        }
        
        Button(onClick = onChatScreenButtonClicked,modifier= Modifier
            .fillMaxWidth()
            .height(64.dp)) {
            Text(text = stringResource(id = R.string.go_to_chat),style=TextStyle(fontSize = 14.sp), fontWeight = FontWeight.Bold,modifier=Modifier)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeDeviceItem(
    modifier: Modifier = Modifier,
    name: String?,
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
fun BluetoothDeviceItem(
    modifier: Modifier = Modifier,
    name: String?,
    macAddress: String,
    borderColor: Color = Color.LightGray,
    borderWidth: Dp = 0.4.dp
) {
    Surface(
        modifier = modifier
            .height(64.dp)
            .fillMaxWidth()
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name ?: "no name",
                modifier = Modifier.weight(0.6f),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "mac : $macAddress",
                style = TextStyle(color = Color.Gray, fontSize = 12.sp),
                modifier = Modifier
                    .align(Alignment.Top)
                    .weight(0.4f)
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun ConnectScreenPreview() {
    BluetoothChatTheme {
        ConnectScreen(onBluetoothDeviceScanRequest = {}, onDeviceConnected = {}, onChatScreenNavigateRequested = {})
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceItemPreview() {
    BluetoothChatTheme {
        BluetoothDeviceItem(
            name = "TEST1".repeat(10),
            macAddress = "12:34:56:78:90"
        )
    }
}
