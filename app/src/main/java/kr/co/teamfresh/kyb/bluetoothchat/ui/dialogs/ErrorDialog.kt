package kr.co.teamfresh.kyb.bluetoothchat.ui.dialogs


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kr.co.teamfresh.kyb.bluetoothchat.R
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme

@Composable
fun ErrorDialog(modifier: Modifier =Modifier,onDismissRequest: () -> Unit){
    Dialog(onDismissRequest = onDismissRequest) {
        Card(modifier=modifier) {
            Column(modifier=Modifier.padding(16.dp),verticalArrangement = Arrangement.SpaceBetween) {
                Text(text=stringResource(id = R.string.error_occurred),fontWeight= FontWeight.Bold)
                Text(modifier=Modifier.padding(top=8.dp),text = stringResource(id = R.string.error_msg))
                Button(modifier= Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .padding(top = 22.dp),onClick = onDismissRequest) {
                    Text(text = stringResource(id = R.string.ok))
                }

            }
            
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorDialogPreview(){
    BluetoothChatTheme {
        ErrorDialog {

        }
    }
}