package kr.co.teamfresh.kyb.bluetoothchat.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
){

    private val _state = MutableStateFlow(BluetoothState.STATE_NONE)
    val state: StateFlow<BluetoothState> = _state
    private var mConnectedThread: ConnectedThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mAcceptThread: AcceptThread? = null

    private val myUUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val NAME = "BluetoothChat"

    init {
        start()
    }

    private fun start() {

        Log.d(TAG, "start :")

        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        if (mAcceptThread == null) {
            mAcceptThread = AcceptThread().apply { start() }
        }

    }


    @SuppressLint("MissingPermission")
    fun connect(deviceAddress: String) {
        Log.d(TAG, "connect to: " + deviceAddress)

        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()

        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)

        if(device.bondState==BluetoothDevice.BOND_BONDED){
            try {
                mConnectThread = ConnectThread(myUUID, device).apply {
                    start()
                }
            } catch (e: Exception) {
                Log.d(TAG, "connect fail : $e")

            }
        }else{
            device.createBond()
        }
    }

    fun connected(socket: BluetoothSocket, device: BluetoothDevice) {

        Log.d(TAG, "connected $device")

        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        if (mAcceptThread != null) {
            mAcceptThread!!.cancel()
            mAcceptThread = null
        }
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
        if (mAcceptThread != null) {
            mAcceptThread!!.cancel()
            mAcceptThread = null
        }
    }


    @SuppressLint("MissingPermission") // 이거는 앞에서 미리 검사할건데 나중에 합치는게 좋을듯
    fun getPairedDeviceList(): List<BluetoothDevice> {

        return bluetoothAdapter.bondedDevices.toList()

    }


    @SuppressLint("MissingPermission")
    private inner class AcceptThread : Thread() {
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, myUUID)
        }
        override fun run() {

            name = "AcceptThread"
            Log.d(TAG, "Socket's accept() method start")
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }

                socket?.also {
                    //manageMyConnectedSocket(it)
                    connected(it, it.remoteDevice)
                    mmServerSocket?.close()
                    shouldLoop = false
                }
            }

        }

        fun cancel() {
            try {
                mmServerSocket?.close()
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
            device.createInsecureRfcommSocketToServiceRecord(myUUID)
        }

        override fun run() {
            Log.d(
                TAG,
                "Begin ConnectThread ${device.bondState == BluetoothDevice.BOND_BONDED} | $myUUID"
            )
//            if(device.bondState==BluetoothDevice.BOND_BONDED){
                connectSocket?.connect()
//            }else{
//                val res=device.createBond()
//                Log.d(TAG,res.toString())
//                if(res) connectSocket?.connect()
//            }
            Log.d(TAG, "Connect success")
            _state.value= BluetoothState.STATE_CONNECTED
        }

        fun cancel() {
            try {
                connectSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect" + connectSocket + " socket failed", e)
            }

        }


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


}