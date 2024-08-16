package com.gogumac.bluetoothchat.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import java.util.concurrent.Executor


private const val TAG = "BLUETOOTH_DEBUG_TAG"

const val MESSAGE_READ = 0
const val MESSAGE_WRITE = 1
const val MESSAGE_TOAST = 2

@SuppressLint("MissingPermission")
class BluetoothService(
    private val bluetoothAdapter: BluetoothAdapter,
    private val activity: ComponentActivity? = null
) : DefaultLifecycleObserver {

    private val _state = MutableStateFlow(BluetoothState.STATE_NONE)
    val state: StateFlow<BluetoothState> = _state

    private val pairingState = MutableStateFlow(BluetoothState.STATE_NONE)

    private val myUUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val NAME = "BluetoothChat"

    private val _discoveredDevices = MutableStateFlow(mutableSetOf<BluetoothDevice>())
    val discoveredDevices: StateFlow<MutableSet<BluetoothDevice>> = _discoveredDevices
    private val filter = IntentFilter().apply {
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
    }

    private var serverSocket: BluetoothServerSocket? = null
    private var connectSocket: BluetoothSocket? = null
    private val _connectedDevice: MutableStateFlow<BluetoothDevice?> = MutableStateFlow(null)
    val connectedDevice: StateFlow<BluetoothDevice?> = _connectedDevice

    private val _pairedDeviceList = MutableStateFlow(bluetoothAdapter.bondedDevices.toList())
    val pairedDeviceList: StateFlow<List<BluetoothDevice>> = _pairedDeviceList

    private val _messageFlow = MutableSharedFlow<String>(1)
    val messageFlow: SharedFlow<String> = _messageFlow.asSharedFlow()

    private val connectingScope = CoroutineScope(Job() + Dispatchers.IO)
    private val connectingDeviceJobMap = mutableMapOf<String, Job>()

    val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder().addServiceUuid(
        ParcelUuid(myUUID), null
    ).build()

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

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = if (Build.VERSION.SDK_INT >= 33) p1.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    ) else p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    Log.d(TAG, "pairedDevice : ${device?.name} ${device?.address}")

                    val bondState =
                        p1.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)

                    when (bondState) {
                        BluetoothDevice.BOND_NONE -> {
                            Log.d(TAG, "none")
                            pairingState.value = BluetoothState.STATE_NONE
                        }

                        BluetoothDevice.BOND_BONDING -> {
                            Log.d(TAG, "bonding")
                            pairingState.value = BluetoothState.STATE_BONDING
                        }

                        BluetoothDevice.BOND_BONDED -> {
                            Log.d(TAG, "bonded")
                            pairingState.value = BluetoothState.STATE_BONDED
                            device?.let {
                                _pairedDeviceList.value = pairedDeviceList.value + device
                            }
                        }
                    }
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device = if (Build.VERSION.SDK_INT >= 33) p1.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    ) else p1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device?.address == connectedDevice.value?.address) {
                        finishConnect()
                    }
                }
            }
        }
    }


    private lateinit var bluetoothScanLauncher: ActivityResultLauncher<Intent>
    private val pairingRequest: AssociationRequest =
        AssociationRequest.Builder().addDeviceFilter(deviceFilter).build()
    private var deviceManager: CompanionDeviceManager =
        activity?.getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    private val discoveringCallback: CompanionDeviceManager.Callback =
        object : CompanionDeviceManager.Callback() {
            override fun onAssociationPending(intentSender: IntentSender) {
                super.onAssociationPending(intentSender)
                Toast.makeText(activity, "associate pending", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: CharSequence?) {
                //TODO("Not yet implemented")
            }

            override fun onDeviceFound(intentSender: IntentSender) {
                super.onDeviceFound(intentSender)
                Toast.makeText(activity, "device found", Toast.LENGTH_SHORT).show()
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
        CoroutineScope(Dispatchers.Main).launch {
            _connectedDevice.collect {
                Log.d(TAG, "connectedDevice changed : $it, ${it?.name}")
            }
        }
    }

    override fun onCreate(owner: LifecycleOwner) {
        activity?.let {
            it.registerReceiver(receiver, filter)
            bluetoothScanLauncher =
                it.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
        }
        super.onCreate(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity?.unregisterReceiver(receiver)
        connectingScope.cancel()
        super.onDestroy(owner)
    }

    fun setBluetoothDiscoverable() {
        val discoverableIntent: Intent =
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30)
            }
        bluetoothScanLauncher.launch(discoverableIntent)
    }

    fun startDiscovering(activity: Activity) {
        Log.d(
            "checkfor",
            "discovering state : ${bluetoothAdapter.isDiscovering} findStart!"
        )

        if (!bluetoothAdapter.isDiscovering) {
            _state.value = BluetoothState.STATE_DISCOVERING
            val executor: Executor = Executor { it.run() }
            if(Build.VERSION.SDK_INT>=33){
                deviceManager.associate(pairingRequest, executor, discoveringCallback)
            }else{
                deviceManager.associate(pairingRequest,discoveringCallback,null)
            }

            //val res = bluetoothAdapter.startDiscovery()
            //Log.d("checkfor", "start res: $res")

            activity.registerReceiver(receiver, filter)
        }
    }

    fun requestPairing(address: String): Flow<BluetoothState> {
        val device = bluetoothAdapter.getRemoteDevice(address)
        device.createBond()
        pairingState.value = BluetoothState.STATE_START_BOND
        return pairingState
    }


    suspend fun openServerSocket() = withContext(Dispatchers.IO) {

        serverSocket?.close()
        connectSocket?.close()

        try {
            serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, myUUID)
            Log.d(TAG, "open ServerSocket : $serverSocket")
            _state.value = BluetoothState.STATE_OPEN_SERVER_SOCKET
            connectSocket = serverSocket?.accept()

            connectSocket?.also {
                serverSocket?.close()
                _connectedDevice.value = it.remoteDevice
                _state.value = BluetoothState.STATE_CONNECTED
                Log.d(TAG, "connect success.\n connected with ${it.remoteDevice}")
                val device = it.remoteDevice
                if (device.address in connectingDeviceJobMap) {
                    connectingDeviceJobMap[device.address]?.cancel()
                }
                connectingDeviceJobMap[device.address] =
                    connectingScope.launch { listenMessage(it) }

            }
        } catch (e: IOException) {
            Log.e(TAG, "open ServerSocket fail : $e")
            return@withContext false
        }
        return@withContext true
    }

    fun closeServerSocket() {
        if (serverSocket != null) {
            Log.d(TAG, "close ServerSocket : $serverSocket")
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

        val device = bluetoothAdapter.getRemoteDevice(address)
        connectSocket =
            device.createRfcommSocketToServiceRecord(myUUID)
        try {
            connectSocket?.connect()
            _connectedDevice.value = connectSocket?.remoteDevice
            Log.d(TAG, "connect success.\n connected with $device")
            _state.value = BluetoothState.STATE_CONNECTED

            if (address in connectingDeviceJobMap) {
                connectingDeviceJobMap[address]?.cancel()
            }
            connectingDeviceJobMap[address] =
                connectingScope.launch { listenMessage(connectSocket!!) }

        } catch (e: IOException) {
            Log.e(TAG, "connect fail : $e")
            return@withContext false
        }
        return@withContext true
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
                    _messageFlow.emit(String(buffer, 0, numBytes))
                    Log.d(TAG, "listenMessage : $buffer")
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

    fun finishConnect(): Boolean {
        try {
            connectedDevice.value?.address?.let {
                if (it in connectingDeviceJobMap) {
                    connectingDeviceJobMap[it]?.cancel()
                }
            }
            _connectedDevice.value = null
            _state.value = BluetoothState.STATE_CLOSE_CONNECT
            connectSocket?.close()

        } catch (e: IOException) {
            Log.e(TAG, "Could not close the connect socket", e)
            return false
        }
        return true
    }

    fun finishDiscovering() {
        if (bluetoothAdapter.isDiscovering) {
            val res = bluetoothAdapter.cancelDiscovery()
            if (res) {
                _state.value = BluetoothState.STATE_DISCOVERING_FINISHED
            }

        }
    }


}