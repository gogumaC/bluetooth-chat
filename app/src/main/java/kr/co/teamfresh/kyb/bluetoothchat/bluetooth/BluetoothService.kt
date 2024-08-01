package kr.co.teamfresh.kyb.bluetoothchat.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


private const val TAG = "BLUETOOTH_DEBUG_TAG"

const val MESSAGE_READ = 0
const val MESSAGE_WRITE = 1
const val MESSAGE_TOAST = 2

@SuppressLint("MissingPermission")
class BluetoothService(
    private val handler: Handler,
    val bluetoothAdapter: BluetoothAdapter,
    private val activity: ComponentActivity? = null
) : DefaultLifecycleObserver {

    private val _state = MutableStateFlow(BluetoothState.STATE_NONE)
    val state: StateFlow<BluetoothState> = _state
//    private var mConnectedThread: ConnectedThread? = null
//    private var mConnectThread: ConnectThread? = null
//    private var mAcceptThread: AcceptThread? = null

    private val myUUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val NAME = "BluetoothChat"

    private val _discoveredDevices = MutableStateFlow(mutableSetOf<BluetoothDevice>())
    val discoveredDevices: StateFlow<MutableSet<BluetoothDevice>> = _discoveredDevices
    private val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    }

    private var serverSocket: BluetoothServerSocket? = null
    private var connectSocket: BluetoothSocket? = null
    private val _connectedevice: MutableStateFlow<BluetoothDevice?> = MutableStateFlow(null)
    val connectedDevice: StateFlow<BluetoothDevice?> = _connectedevice

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= 33) p1.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    ) else p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    Log.d(TAG, "foundedDevice : ${device?.name} ${device?.address}")
                    device?.let {
                        _discoveredDevices.value = mutableSetOf<BluetoothDevice>().apply {
                            addAll(_discoveredDevices.value)
                            add(device)
                        }
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _state.value = BluetoothState.STATE_DISCOVERING

                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _state.value = BluetoothState.STATE_DISCOVERING_FINISHED

                }
            }
        }
    }

    init {
        activity?.lifecycle?.addObserver(this)
        //블루투스가 활성화 되어있는지 확인
        if (!bluetoothAdapter.isEnabled) {
            _state.value = BluetoothState.STATE_DISABLE
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            //bluetoothEnableLauncher.launch(enableBtIntent)
        }
        CoroutineScope(Dispatchers.Main).launch {
            _state.collect {
                Log.d("checkfor", "state changed : $it")
            }
        }
        //start()
    }

    override fun onCreate(owner: LifecycleOwner) {
        activity?.registerReceiver(receiver, filter)
        super.onCreate(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity?.unregisterReceiver(receiver)
        super.onDestroy(owner)
    }

    fun startDiscovering(activity: Activity) {
        Log.d(
            "checkfor",
            "discovering state : ${bluetoothAdapter.isDiscovering} findStart!"
        )

        if (!bluetoothAdapter.isDiscovering) {
            _state.value = BluetoothState.STATE_DISCOVERING
            val res = bluetoothAdapter.startDiscovery()
            Log.d("checkfor", "start res: $res")

            activity.registerReceiver(receiver, filter)
        }
    }

    suspend fun openServerSocket() = withContext(Dispatchers.IO) {

        if (serverSocket != null) {
            serverSocket?.close()
        }
        if (connectSocket != null) {
            connectSocket?.close()
        }
        try {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, myUUID)
            Log.d(TAG, "open ServerSocket : $serverSocket")
            _state.value = BluetoothState.STATE_OPEN_SERVER_SOCKET
            connectSocket = serverSocket?.accept()

            connectSocket?.also {
                serverSocket?.close()
                _connectedevice.value = it.remoteDevice
                _state.value = BluetoothState.STATE_CONNECTED
            }
        } catch (e: IOException) {
            Log.e(TAG, "open ServerSocket fail : $e")
        }
    }

    fun closeServerSocket() {
        if (serverSocket != null) {
            serverSocket?.close()
        }
    }

    suspend fun requestConnect(address: String) = withContext(Dispatchers.IO) {
        if (connectSocket != null) {
            connectSocket?.close()
        }
        if (serverSocket != null) {
            serverSocket?.close()
        }
        connectSocket =
            bluetoothAdapter.getRemoteDevice(address).createRfcommSocketToServiceRecord(myUUID)
        try {
            connectSocket?.connect()
            _state.value = BluetoothState.STATE_CONNECTED
        } catch (e: IOException) {
            Log.e(TAG, "connect fail : $e")
        }
    }

    private suspend fun listenMessage(connectSocket: BluetoothSocket) =
        withContext(Dispatchers.IO) {
            var numBytes: Int
            val buffer = ByteArray(1024)
            val inputStream = connectSocket.inputStream

            while (true) {
                numBytes = try {
                    inputStream.read(buffer)
                } catch (e: IOException) {
                    Log.d(TAG, "Input Stream was disconnected", e)
                    break
                }

                if (numBytes > 0) {
                    val readMsg = handler.obtainMessage(MESSAGE_READ, numBytes, -1, buffer)
                    readMsg.sendToTarget()
                }

            }
        }

    suspend fun sendMessage(msg: ByteArray) = withContext(Dispatchers.IO) {
        val outputStream = connectSocket?.outputStream
        try {
            outputStream?.write(msg)
        } catch (e: IOException) {
            Log.e(TAG, "Error occurred when sending data", e)
        }
    }

    fun cancelConnect() {
        try {
            connectSocket?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
        }
    }

    fun finishDiscovering() {
        if (bluetoothAdapter.isDiscovering) {
            val res = bluetoothAdapter.cancelDiscovery()
            if (res) {
                _state.value = BluetoothState.STATE_DISCOVERING_FINISHED
            }

        }
    }

//    fun start() {
//
//        Log.d(TAG, "start :")
//
//        if (mConnectThread != null) {
//            mConnectThread!!.cancel()
//            mConnectThread = null
//        }
//        if (mConnectedThread != null) {
//            mConnectedThread!!.cancel()
//            mConnectedThread = null
//        }
//        if (mAcceptThread == null) {
//            mAcceptThread = AcceptThread().apply { start() }
//        }
//    }
//
//
//    fun connect(deviceAddress: String) {
//        Log.d(TAG, "connect to: " + deviceAddress)
//
//        if (mConnectThread != null) {
//            mConnectThread!!.cancel()
//            mConnectThread = null
//        }
//        if (mConnectedThread != null) {
//            mConnectedThread!!.cancel()
//            mConnectedThread = null
//        }
//        if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()
//
//        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
//
//        if(device.bondState==BluetoothDevice.BOND_BONDED){
//
//            CoroutineScope(Dispatchers.IO).launch {
//                val res=connectWithDevice(device)
//                Log.d(TAG, "connect res : $res")
//            }
//        }else{
//            device.createBond()
//        }
//
//    }


//    private suspend fun connectWithDevice(device: BluetoothDevice):Boolean = try{
//        withContext(Dispatchers.IO){
//            async {
//                val socket = device.createRfcommSocketToServiceRecord(myUUID)
//                socket.connect()
//                connected(socket, device)
//
//            }.await()
//            true
//        }
//    }catch (e:IOException){
//        Log.e(TAG, "connect fail : $e")
//        false
//    }

//    fun connected(socket: BluetoothSocket, device: BluetoothDevice) {
//
//        Log.d(TAG, "connected $device")
//
//        if (mConnectThread != null) {
//            mConnectThread!!.cancel()
//            mConnectThread = null
//        }
//        if (mConnectedThread != null) {
//            mConnectedThread!!.cancel()
//            mConnectedThread = null
//        }
//        if (mAcceptThread != null) {
//            mAcceptThread!!.cancel()
//            mAcceptThread = null
//        }
//        mConnectedThread = ConnectedThread(socket).apply {
//            start()
//        }
//    }

//    fun wirte(out: ByteArray) {
//        mConnectedThread?.write(out)
//    }
//
//    fun stop() {
//        Log.d(TAG, "stop")
//
//        if (mConnectThread != null) {
//            mConnectThread!!.cancel();
//            mConnectThread = null;
//        }
//
//        if (mConnectedThread != null) {
//            mConnectedThread!!.cancel();
//            mConnectedThread = null;
//        }
//        if (mAcceptThread != null) {
//            mAcceptThread!!.cancel()
//            mAcceptThread = null
//        }
//    }

    fun getPairedDeviceList(): List<BluetoothDevice> {
        return bluetoothAdapter.bondedDevices.toList()
    }

//    private inner class AcceptThread : Thread() {
//        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
//            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, myUUID)
//        }
//        override fun run() {
//
//            name = "AcceptThread"
//            Log.d(TAG, "Socket's accept() method start")
//            var shouldLoop = true
//            while (shouldLoop) {
//                val socket: BluetoothSocket? = try {
//                    mmServerSocket?.accept()
//                } catch (e: IOException) {
//                    Log.e(TAG, "Socket's accept() method failed", e)
//                    shouldLoop = false
//                    null
//                }
//
//                socket?.also {
//                    //manageMyConnectedSocket(it)
//                    connected(it, it.remoteDevice)
//                    mmServerSocket?.close()
//                    shouldLoop = false
//                }
//            }
//
//        }
//
//        fun cancel() {
//            try {
//                mmServerSocket?.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "Could not close the connect socket", e)
//            }
//        }
//    }
//
//    private inner class ConnectThread(
//        private val myUUID: UUID,
//        private val device: BluetoothDevice
//    ) : Thread() {
//
//        private val connectSocket: BluetoothSocket? by lazy(LazyThreadSafetyMode.NONE) {
//            device.createInsecureRfcommSocketToServiceRecord(myUUID)
//        }
//
//        override fun run() {
//            Log.d(
//                TAG,
//                "Begin ConnectThread ${device.bondState == BluetoothDevice.BOND_BONDED} | $myUUID"
//            )
////            if(device.bondState==BluetoothDevice.BOND_BONDED){
//                try {
//                    connectSocket?.connect()
//                    Log.d(TAG, "Connect success")
//                    _state.value= BluetoothState.STATE_CONNECTED
//                }catch (e:IOException){
//                    Log.e(TAG, "connect fail : $e")
//                }
////            }else{
////                val res=device.createBond()
////                Log.d(TAG,res.toString())
////                if(res) connectSocket?.connect()
////            }
//
//        }
//
//        fun cancel() {
//            try {
//                connectSocket?.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "close() of connect" + connectSocket + " socket failed", e)
//            }
//        }
//    }
//
//    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
//        private val mmInStream: InputStream = mmSocket.inputStream
//        private val mmOutStream: OutputStream = mmSocket.outputStream
//        private val mmBuffer: ByteArray = ByteArray(1024)
//
//        override fun run() {
//            var numBytes: Int
//
//            while (true) {
//                numBytes = try {
//                    mmInStream.read(mmBuffer)
//                } catch (e: IOException) {
//                    Log.d(TAG, "Input Stream was disconnected", e)
//                    break
//                }
//
//                val readMsg = handler.obtainMessage(
//                    MESSAGE_READ, numBytes, -1, mmBuffer
//                )
//                readMsg.sendToTarget()
//            }
//        }
//
//        fun write(bytes: ByteArray) {
//            try {
//                mmOutStream.write(bytes)
//            } catch (e: IOException) {
//                Log.e(TAG, "Error occurred when sending data", e)
//
//                val writeErrorMsg = handler.obtainMessage(MESSAGE_TOAST)
//                val bundle = Bundle().apply {
//                    putString("toast", "Couldn't send data to the other device")
//                }
//                writeErrorMsg.data = bundle
//                handler.sendMessage(writeErrorMsg)
//                return
//            }
//
//            val writtenMsg = handler.obtainMessage(
//                MESSAGE_WRITE, -1, -1, mmBuffer
//            )
//            writtenMsg.sendToTarget()
//        }
//
//        fun cancel() {
//            try {
//                mmSocket.close()
//            } catch (e: IOException) {
//                Log.e(TAG, "Could not close the connect socket", e)
//            }
//        }
//    }


}