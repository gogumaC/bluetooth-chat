package kr.co.teamfresh.kyb.bluetoothchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatScreenViewModel:ViewModel() {
    private val _messageList = MutableStateFlow<List<String>>(emptyList())
    val messageList = _messageList.asStateFlow()

    private val _connectedDevice = MutableStateFlow<String>("")
    val connectedDevice = _connectedDevice.asStateFlow()

    private val _text=MutableStateFlow("")
    val text=_text.asStateFlow()

    fun sendMessage() {
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