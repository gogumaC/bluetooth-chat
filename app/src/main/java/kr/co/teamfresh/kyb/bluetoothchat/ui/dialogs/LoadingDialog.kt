package kr.co.teamfresh.kyb.bluetoothchat.ui.dialogs

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme

@Composable
fun LoadingDialog(modifier: Modifier, onDismissRequest: () -> Unit, text: String) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier = modifier) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                Text(text = text)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingDialogPreview() {
    BluetoothChatTheme {
        LoadingDialog(modifier = Modifier, onDismissRequest = { /*TODO*/ }, text = "loading")
    }
}