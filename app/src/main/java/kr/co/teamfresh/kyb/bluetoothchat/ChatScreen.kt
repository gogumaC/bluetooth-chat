package kr.co.teamfresh.kyb.bluetoothchat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme
import androidx.compose.foundation.lazy.items

@Composable
fun ChatScreen(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Blue)
    ) {
        Column {
            LazyColumn(modifier = Modifier, verticalArrangement = Arrangement.spacedBy(5.dp)){
                items(List(5){it.toString()}){
                    TalkUnit(text = it)
                }
            }
            ChatInput(
                text = text,
                onValueChanged = { text = it },
                modifier = Modifier.background(Color.Blue)
            )
        }
    }
}


@Composable
fun TalkUnit(text:String,modifier: Modifier=Modifier){
    Box(modifier = modifier.background(Color.LightGray)){
        Text(modifier=Modifier.padding(10.dp),text=text)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(
    text: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val minHeight = 48.dp
    val maxHeight = 150.dp
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        TextField(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = minHeight, max = maxHeight),
            value = text,
            onValueChange = onValueChanged
        )
        IconButton(modifier = Modifier
            .background(Color.Yellow)
            .width(minHeight), onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Filled.Send, contentDescription = "")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TalkUnitPreview(){
    BluetoothChatTheme {
        TalkUnit(text = "HELLO".repeat(20))
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun ChatScreenPreview() {
    BluetoothChatTheme {
        ChatScreen(Modifier.fillMaxSize())
    }
}