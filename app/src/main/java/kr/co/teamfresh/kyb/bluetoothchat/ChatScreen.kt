package kr.co.teamfresh.kyb.bluetoothchat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
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

    Surface(
        modifier = modifier.padding(16.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Text("연결 가능한 기기 목록")
            Spacer(modifier = Modifier.size(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(service?.getPairedDeviceList() ?: listOf()) {
                    Button(onClick = {
                        service?.start()
                        service?.connect(it.address)
                    }, modifier = Modifier.fillMaxWidth()) {
                        Column() {
                            Text(text = it.name)
                            Text(text = it.address)
                        }
                    }
                }
            }
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

