package kr.co.teamfresh.kyb.bluetoothchat.ui.theme

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

private const val TAG="MY_APP_DEBUG_TAG"

const val MESSAGE_READ=0
const val MESSAGE_WRITE=1
const val MESSAGE_TOAST=2

class BluetoothChatService(private val handler: Handler,val context: Context, val bluetoothAdapter:BluetoothAdapter) {

    fun write(bytes:ByteArray){
        //val connectThread=ConnectedThread(bluetoothSocket)
        //connectThread.write(bytes)
    }

    @SuppressLint("MissingPermission") // 이거는 앞에서 미리 검사할건데 나중에 합치는게 좋을듯
    fun getPairedDeviceList():Set<BluetoothDevice>
    {
//        if(!bluetoothAdapter.isEnabled)return

        return bluetoothAdapter.bondedDevices

//        Log.d("BLUETOOTH_LIST",pairedDevices.toString())
    }

    private inner class ConnectedThread(private val mmSocket:BluetoothSocket):Thread(){
        private val mmInStream:InputStream=mmSocket.inputStream
        private val mmOutStream:OutputStream=mmSocket.outputStream
        private val mmBuffer:ByteArray=ByteArray(1024)

        override fun run() {
            var numBytes:Int

            while(true){
                numBytes=try{
                    mmInStream.read(mmBuffer)
                }catch(e:IOException){
                    Log.d(TAG,"Input Stream was disconnected",e)
                    break
                }

                val readMsg=handler.obtainMessage(
                    MESSAGE_READ,numBytes,-1,mmBuffer
                )
                readMsg.sendToTarget()
            }
        }

        fun write(bytes:ByteArray){
            try{
                mmOutStream.write(bytes)
            }catch(e:IOException){
                Log.e(TAG,"Error occurred when sending data",e)

                val writeErrorMsg=handler.obtainMessage(MESSAGE_TOAST)
                val bundle= Bundle().apply{
                    putString("toast","Couldn't send data to the other device")
                }
                writeErrorMsg.data=bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            val writtenMsg=handler.obtainMessage(
                MESSAGE_WRITE,-1,-1,mmBuffer
            )
            writtenMsg.sendToTarget()
        }

        fun cancel(){
            try{
                mmSocket.close()
            }catch (e:IOException){
                Log.e(TAG,"Could not close the connect socket",e)
            }
        }
    }




}