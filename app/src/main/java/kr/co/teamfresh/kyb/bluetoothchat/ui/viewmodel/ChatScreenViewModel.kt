package kr.co.teamfresh.kyb.bluetoothchat.ui.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.BluetoothService
import kr.co.teamfresh.kyb.bluetoothchat.data.Device.Companion.toDevice
import kr.co.teamfresh.kyb.bluetoothchat.data.Message

class ChatScreenViewModel(val bluetoothService: BluetoothService? = null) : ViewModel() {

    private val _messageList = MutableStateFlow(listOf<Message>())
    val messageList: StateFlow<List<Message>> = _messageList.asStateFlow()

    //private val _connectedDevice = MutableStateFlow<Device>(testDevice)
    val connectedDevice=bluetoothService?.connectedDevice?.value?.toDevice()

    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()

    init {
        viewModelScope.launch {
            listenMessage()
        }
    }


    fun sendMessage() {
        viewModelScope.launch {
            bluetoothService?.sendMessage(_text.value.toByteArray())

        }
        val newMessage = Message(text = _text.value, isMine = true)
        _messageList.value += newMessage
        _text.value = ""
    }


    fun setText(text: String) {
        _text.value = text
    }

    private suspend fun listenMessage() = withContext(Dispatchers.IO) {
        bluetoothService?.messageFlow?.collect { msg ->
            _messageList.value += Message(text = msg, device = connectedDevice, isMine = false)
        }

    }


    fun connectDevice(deviceName: String) {}
    fun disconnectDevice() {}


}