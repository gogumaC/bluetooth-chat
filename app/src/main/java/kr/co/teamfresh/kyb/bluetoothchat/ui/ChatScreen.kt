package kr.co.teamfresh.kyb.bluetoothchat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.TalkBackground

@Composable
fun ChatScreen(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Blue)
    ) {
        Column() {
            LazyColumn(modifier = Modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(List(5) { it.toString() }) {
                    Talk(content = "$it Hello".repeat(20),defaultSize=48.dp)
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
fun Talk(content:String,modifier: Modifier = Modifier,defaultSize:Dp=48.dp) {
    Row(modifier = modifier) {
        DeviceBadge(deviceName = content, size = defaultSize)
        Spacer(modifier = Modifier.width(6.dp))
        TalkUnit(content,defaultSize=defaultSize)
    }
}

@Composable
fun DeviceBadge(deviceName: String,modifier: Modifier = Modifier,size:Dp=48.dp) {
    Card(modifier = modifier.size(size)) {
        Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                modifier = Modifier,
                text = deviceName[0].toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

    }

}

@Composable
fun TalkUnit(text: String, modifier: Modifier = Modifier,defaultSize: Dp=48.dp) {
    Card(modifier = modifier.heightIn(min=defaultSize),shape= RoundedCornerShape(8.dp)) {
        Text(modifier = Modifier.padding(10.dp), text = text)
    }
}

@Composable
fun ChatInput(
    text: TextFieldValue,
    onValueChanged: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    minHeight:Dp=48.dp,
    maxHeight:Dp=150.dp
) {
    Column(modifier=modifier.fillMaxWidth()) {
        HorizontalDivider(modifier=Modifier.height(1.dp))
        Row(
            modifier = Modifier
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            //
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = minHeight, max = maxHeight),
                value = text,
                onValueChange = onValueChanged
            )
            Spacer(modifier=Modifier.width(10.dp))
            IconButton(modifier = Modifier
                .background(Color.Yellow)
                .width(minHeight), onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Default.Email, contentDescription = "")
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun TalkPreview() {
    BluetoothChatTheme {
        Talk("Hello")
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun ChatScreenPreview() {
    BluetoothChatTheme {
        ChatScreen(Modifier.fillMaxSize())
    }
}