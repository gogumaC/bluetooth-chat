package kr.co.teamfresh.kyb.bluetoothchat.ui.viewmodel

import android.bluetooth.BluetoothDevice
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.BluetoothService
import kr.co.teamfresh.kyb.bluetoothchat.data.Device
import kr.co.teamfresh.kyb.bluetoothchat.data.Device.Companion.toDevice
import kr.co.teamfresh.kyb.bluetoothchat.data.Message

class ChatScreenViewModel(private val bluetoothService: BluetoothService? = null) : ViewModel() {

    private val _messageList = MutableStateFlow(listOf<Message>())
    val messageList: StateFlow<List<Message>> = _messageList.asStateFlow()

    val connectedDevice:StateFlow<Device?> = bluetoothService?.connectedDevice?.transform {
        emit(it?.toDevice())
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(),null)?: MutableStateFlow(null).asStateFlow()


    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()

    init {
        viewModelScope.launch {
            listenMessage()
        }
    }

    fun sendMessage() {
        if(_text.value.isEmpty()) return
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
        bluetoothService?.messageFlow?.drop(1)?.collect { msg ->
            if (msg.isNotEmpty()) _messageList.value += Message(
                text = msg,
                device = connectedDevice.value,
                isMine = false
            )
        }
    }

    fun connectDevice(deviceName: String) {}
    fun disconnectDevice() {}
}