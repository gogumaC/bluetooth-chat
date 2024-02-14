package kr.co.teamfresh.kyb.bluetoothchat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme

@Composable
fun ChatScreen(modifier: Modifier=Modifier){
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.background
    ) {
        Box(){
            Button(onClick = { /*TODO*/ },modifier=Modifier.size(width=160.dp,height=100.dp)) {
                Text(text = "Send Hello")
            }
        }

    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun ChatScreenPreview(){
    BluetoothChatTheme {
        ChatScreen(Modifier.fillMaxSize())
    }
}