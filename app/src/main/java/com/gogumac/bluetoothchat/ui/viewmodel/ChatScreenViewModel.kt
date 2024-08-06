package com.gogumac.bluetoothchat.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.gogumac.bluetoothchat.bluetooth.BluetoothService
import com.gogumac.bluetoothchat.data.Device
import com.gogumac.bluetoothchat.data.Device.Companion.toDevice
import com.gogumac.bluetoothchat.data.Message

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
        bluetoothService?.messageFlow?.collect { msg ->
            if (msg.isNotEmpty()) _messageList.value += Message(
                text = msg,
                device = connectedDevice.value,
                isMine = false
            )
        }
    }

}