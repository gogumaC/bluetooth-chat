package com.gogumac.bluetoothchat.data

data class Message(val text: String, val device: Device?=null, val isMine: Boolean)