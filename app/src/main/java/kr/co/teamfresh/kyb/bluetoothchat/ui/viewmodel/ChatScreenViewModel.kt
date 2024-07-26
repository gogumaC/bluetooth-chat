package kr.co.teamfresh.kyb.bluetoothchat.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kr.co.teamfresh.kyb.bluetoothchat.data.Message

class ChatScreenViewModel:ViewModel() {
    private val _messageList = MutableStateFlow<List<Message>>(emptyList())
    val messageList : StateFlow<List<Message>> = _messageList.asStateFlow()

    private val _connectedDevice = MutableStateFlow<String>("")
    val connectedDevice = _connectedDevice.asStateFlow()

    private val _text=MutableStateFlow("")
    val text=_text.asStateFlow()

    fun sendMessage() {
        val newMessage=Message(_text.value)
        _messageList.value += newMessage
        _text.value=""
    }

    fun getMessages() {

    }

    fun setText(text:String){
        _text.value=text
    }


    fun connectDevice(deviceName: String) {}
    fun disconnectDevice() {}





}