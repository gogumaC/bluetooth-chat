package kr.co.teamfresh.kyb.bluetoothchat.data

data class Message(val text: String,val device:Device?=null, val isMine: Boolean)