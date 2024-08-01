package kr.co.teamfresh.kyb.bluetoothchat.data

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Device(
    val name: String,
    val mac: String,
    val color: Color,
    val image: ImageVector? = null
) {

    companion object{
        @SuppressLint("MissingPermission")
        fun BluetoothDevice.toDevice(): Device =
            Device(name = name, mac = address, color = Color.Cyan, image = null)
    }

}
