package com.gogumac.bluetoothchat.ui

import android.bluetooth.BluetoothDevice
import kotlinx.serialization.Serializable

@Serializable
object Connect

@Serializable
object Chat

@Serializable
object Error

@Serializable
object Discovery

@Serializable
object ServerSocketLoading

@Serializable
data class DialogConnectLoading(val deviceAddress: String)

@Serializable
object DialogPairingLoading

@Serializable
object DisconnectAlertDialog

@Serializable
data class DialogSelectConnectAccept(val deviceName:String,val deviceAddress: String)
