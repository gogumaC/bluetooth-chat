package kr.co.teamfresh.kyb.bluetoothchat.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
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
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID


private const val TAG = "MY_APP_DEBUG_TAG"

const val MESSAGE_READ = 0
const val MESSAGE_WRITE = 1
const val MESSAGE_TOAST = 2

@SuppressLint("MissingPermission")
class BluetoothService(
    private val handler: Handler,
    val context: Context,
    val bluetoothAdapter: BluetoothAdapter,
    val activity: ComponentActivity?=null
):DefaultLifecycleObserver{

    private val _state = MutableStateFlow(BluetoothState.STATE_NONE)
    val state: StateFlow<BluetoothState> = _state
    private var mConnectedThread: ConnectedThread? = null
    private var mConnectThread: ConnectThread? = null
    private var mAcceptThread: AcceptThread? = null

    private val myUUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val NAME = "BluetoothChat"

    val discoveredDevices= mutableStateListOf<BluetoothDevice>()
    val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    }
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= 33) p1.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    ) else p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    Log.d("checkfor", "foundedDevice : ${device?.name} ${device?.address}")
                    device?.let {
                        discoveredDevices.add(device)
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _state.value=BluetoothState.STATE_DISCOVERING
                    Log.d("checkfor", "start discovering")
//                    bluetoothDiscoveringState = BluetoothChatService.STATE_DISCOVERING
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _state.value=BluetoothState.STATE_DISCOVERING_FINISHED
                    Log.d("checkfor", "finish discovering")
//                    bluetoothDiscoveringState = BluetoothChatService.STATE_DISCOVERING_FINISHED
                }
            }
        }
    }

    init {
        activity?.lifecycle?.addObserver(this)
        //블루투스가 활성화 되어있는지 확인
        if (!bluetoothAdapter.isEnabled) {
            _state.value=BluetoothState.STATE_DISABLE
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            //bluetoothEnableLauncher.launch(enableBtIntent)
        }
        CoroutineScope(Dispatchers.Main).launch{
            _state.collect{
                Log.d("checkfor", "state changed : $it")
            }
        }
        start()
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
            _state.value=BluetoothState.STATE_DISCOVERING
            val res = bluetoothAdapter.startDiscovery()
            Log.d("checkfor", "start res: $res")

            activity.registerReceiver(receiver, filter)
        }
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