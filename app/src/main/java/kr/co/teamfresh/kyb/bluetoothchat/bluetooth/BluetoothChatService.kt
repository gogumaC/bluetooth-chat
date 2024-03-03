package kr.co.teamfresh.kyb.bluetoothchat.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

private const val TAG = "MY_APP_DEBUG_TAG"

const val MESSAGE_READ = 0
const val MESSAGE_WRITE = 1
const val MESSAGE_TOAST = 2

class BluetoothChatService(
    private val handler: Handler,
    val context: Context,
    val bluetoothAdapter: BluetoothAdapter
) {

    private var mConnectedThread: ConnectedThread? = null
    private var mConnectThread: ConnectThread? = null

    private val myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")


    fun start() {
        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

    }

    //@SuppressLint("MissingPermission")
    fun connect(deviceAddress: String) {
        if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()


        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)

        mConnectThread = ConnectThread(myUUID, device).apply {
            start()
        }
    }

    fun connected(socket: BluetoothSocket, device: BluetoothDevice) {
        mConnectedThread = ConnectedThread(socket).apply {
            start()
        }
    }

    fun wirte(out: ByteArray) {
        mConnectedThread?.write(out)
    }

    fun stop() {
        Log.d(TAG, "stop")

        if (mConnectThread != null) {
            mConnectThread!!.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread!!.cancel();
            mConnectedThread = null;
        }
    }


    @SuppressLint("MissingPermission") // 이거는 앞에서 미리 검사할건데 나중에 합치는게 좋을듯
    fun getPairedDeviceList(): List<BluetoothDevice> {

        return bluetoothAdapter.bondedDevices.toList()

    }


    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream = mmSocket.inputStream
        private val mmOutStream: OutputStream = mmSocket.outputStream
        private val mmBuffer: ByteArray = ByteArray(1024)

        override fun run() {
            var numBytes: Int

            while (true) {
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input Stream was disconnected", e)
                    break
                }

                val readMsg = handler.obtainMessage(
                    MESSAGE_READ, numBytes, -1, mmBuffer
                )
                readMsg.sendToTarget()
            }
        }

        fun write(bytes: ByteArray) {
            try {
                mmOutStream.write(bytes)
            } catch (e: IOException) {
                Log.e(TAG, "Error occurred when sending data", e)

                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
                val bundle = Bundle().apply {
                    putString("toast", "Couldn't send data to the other device")
                }
                writeErrorMsg.data = bundle
                handler.sendMessage(writeErrorMsg)
                return
            }

            val writtenMsg = handler.obtainMessage(
                MESSAGE_WRITE, -1, -1, mmBuffer
            )
            writtenMsg.sendToTarget()
        }

        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private inner class ConnectThread(
        private val myUUID: UUID,
        private val device: BluetoothDevice
    ) : Thread() {

        private val connectSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
            device.createRfcommSocketToServiceRecord(myUUID)
        }

        override fun run() {
            try {
                connectSocket?.connect()
                connectSocket?.let {
                    val connectedThread = ConnectedThread(it)
                }
            } catch (e: IOException) {
                connectSocket?.close()
                throw Exception("connect fail")
            }
        }

        fun cancel() {
            try {
                connectSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect" + connectSocket + " socket failed", e)
            }

        }


    }


}