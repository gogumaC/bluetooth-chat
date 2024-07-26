package kr.co.teamfresh.kyb.bluetoothchat.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import kr.co.teamfresh.kyb.bluetoothchat.R
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.TalkBackground
import kr.co.teamfresh.kyb.bluetoothchat.ui.viewmodel.ChatScreenViewModel
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@Composable
fun ChatScreen(modifier: Modifier = Modifier,viewModel:ChatScreenViewModel= ChatScreenViewModel()) {
    val text by viewModel.text.collectAsState()
    val messageList by viewModel.messageList.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            DeviceInfo(
                deviceName = "Hellow",
                deviceImage = ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
                backgroundColor = Color.Yellow
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    top = 10.dp,
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 10.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(messageList) {
                    Talk(
                        content = it.text,
                        isMine = it.isMine,
                        deviceName = it.device?.name.toString(),
                        deviceColor = it.device?.color ?: Color.Green,
                        deviceImage = it.device?.image ?: ImageVector.vectorResource(id = R.drawable.ic_launcher_foreground),
                        defaultSize = 48.dp
                    )
                }
            }

            ChatInput(
                text = text,
                onValueChanged = { viewModel.setText(it) },
                onSendButtonClicked = {
                    viewModel.sendMessage()
                }
            )
        }
    }
}

@Composable
fun DeviceInfo(
    deviceName: String,
    deviceImage: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val appendText = stringResource(id = R.string.connecting)
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(deviceName)
        }
        append(appendText)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(color = MaterialTheme.colorScheme.primary),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = deviceImage,
            contentDescription = "",
            modifier = Modifier
                .padding(top = 10.dp, start = 16.dp, bottom = 10.dp)
                .background(color = backgroundColor, shape = CircleShape)
        )
        Text(text = annotatedString, modifier = Modifier.padding(10.dp), color = Color.White)
    }
}

@Composable
fun Talk(
    content: String,
    deviceName: String,
    deviceColor: Color,
    isMine: Boolean,
    modifier: Modifier = Modifier,
    defaultSize: Dp = 48.dp,
    deviceImage: ImageVector?=null,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start) {
        if (isMine) {
            TalkUnit(content, defaultSize = defaultSize)
            Spacer(modifier = Modifier.width(6.dp))
        }

        if (!isMine){
            Column(modifier = Modifier.width(defaultSize), horizontalAlignment =Alignment.CenterHorizontally) {
                DeviceBadge(deviceName = deviceName, deviceColor = deviceColor, deviceImage = deviceImage, size = defaultSize)
                Text(
                    text = deviceName,
                    modifier = Modifier.padding(top = 4.dp),
                    style = TextStyle(fontSize = 12.sp, textAlign = TextAlign.Center),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            TalkUnit(content, defaultSize = defaultSize)
        }
    }
}

@Composable
fun DeviceBadge(
    deviceName: String,
    deviceColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    deviceImage: ImageVector?=null
) {
    Card(modifier = modifier.size(size), border = BorderStroke(1.dp, color = Color.LightGray)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = deviceColor, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            if(deviceImage==null){
                Text(
                    modifier = Modifier,
                    text = deviceName[0].toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }else{
                Image(imageVector = deviceImage, contentDescription = "")
            }
        }
    }
}

@Composable
fun TalkUnit(text: String, modifier: Modifier = Modifier, defaultSize: Dp = 48.dp) {
    Card(modifier = modifier.heightIn(min = defaultSize), shape = RoundedCornerShape(8.dp)) {
        Text(
            modifier = Modifier
                .padding(10.dp)
                .widthIn(max = 250.dp), text = text
        )
    }
}

@Composable
fun ChatInput(
    text: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 48.dp,
    maxHeight: Dp = 150.dp,
    buttonWidth: Dp = 48.dp,
    onSendButtonClicked: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight, max = maxHeight)
    ) {
        HorizontalDivider(modifier = Modifier.height(1.dp))


        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        )
        {
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = minHeight, max = maxHeight),
                value = text,
                onValueChange = onValueChanged,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                modifier = Modifier
                    .width(buttonWidth)
                    .fillMaxHeight()
                    .background(
                        MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ),
                onClick = onSendButtonClicked
            ) {
                Icon(imageVector = Icons.Default.Email, tint = Color.White, contentDescription = "")
            }
        }
    }

}


@Preview(showBackground = true, showSystemUi = false)
@Composable
fun ChatScreenPreview() {
    BluetoothChatTheme {
        ChatScreen(Modifier.fillMaxSize())
    }
}