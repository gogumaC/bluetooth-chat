package kr.co.teamfresh.kyb.bluetoothchat.data

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Device(val name:String,val mac:String,val color: Color,val image: ImageVector?=null)
