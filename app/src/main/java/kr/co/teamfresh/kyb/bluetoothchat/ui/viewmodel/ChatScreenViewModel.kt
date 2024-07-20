package kr.co.teamfresh.kyb.bluetoothchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatScreenViewModel:ViewModel() {
    val _messageList = MutableStateFlow<List<String>>(emptyList())
    val messageList = _messageList.asStateFlow()

    val _connectedDevice = MutableStateFlow<String>("")
    val connectedDevice = _connectedDevice.asStateFlow()

    val _text=MutableStateFlow("")
    val text=_text.asStateFlow()

    fun sendMessage(message: String) {}

    fun getMessages() {

    }


    fun connectDevice(deviceName: String) {}
    fun disconnectDevice() {}





}