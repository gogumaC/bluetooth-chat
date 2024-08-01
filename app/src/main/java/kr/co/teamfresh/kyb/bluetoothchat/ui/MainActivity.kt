package kr.co.teamfresh.kyb.bluetoothchat.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import kr.co.teamfresh.kyb.bluetoothchat.R
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.BluetoothService
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.MESSAGE_READ
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.MESSAGE_TOAST
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.MESSAGE_WRITE
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme
import kr.co.teamfresh.kyb.bluetoothchat.ui.viewmodel.ConnectableDeviceListDialog

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var bluetoothEnableLauncher: ActivityResultLauncher<Intent>
    private lateinit var bluetoothSettingLauncher: ActivityResultLauncher<Intent>
    private lateinit var bluetoothScanLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MESSAGE_READ -> {
                        val readBuf = msg.obj as ByteArray
                    }

                    MESSAGE_WRITE -> {
                        Toast.makeText(this@MainActivity, "Success send data", Toast.LENGTH_SHORT)
                            .show()
                    }

                    MESSAGE_TOAST -> {
                        Toast.makeText(this@MainActivity, "ERROR", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        bluetoothEnableLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
                } else if (result.resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "bluetooth not enable", Toast.LENGTH_SHORT).show()
                }
            }

        bluetoothPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                val deniedList = result.filter { !it.value }.map { it.key }

                if (deniedList.isNotEmpty()) {
                    val map = deniedList.groupBy { permission ->
                        if (shouldShowRequestPermissionRationale(permission)) "DENIED" else "EXPLAINED"
                    }
                    map["DENIED"]?.let {
                        explainBluetoothConnectPermission()
                    }
                }
            }
//        bluetoothSettingLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//
//            }
//        bluetoothScanLauncher =
//            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//
//            }

        //블루투스가 기기에서 지원되는지 확인
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
        }

        //블루투스 권한 확인
        requestBluetoothConnectPermission()

        val service = BluetoothService(mHandler,  bluetoothAdapter!!,this)

        service.getPairedDeviceList()


        setContent {
            val navController = rememberNavController()
            val discoveredDevice = service.discoveredDevices.collectAsState()
            val bluetoothState = service.state.collectAsState()
            val savedBluetoothDevices=service.getPairedDeviceList()
            BluetoothChatTheme {
                NavHost(navController = navController,startDestination=Connect){
                    composable<Connect> {
                        ConnectScreen(
                            modifier = Modifier.fillMaxSize(),
                            deviceList = savedBluetoothDevices,
                            onBluetoothDeviceScanRequest = {
                                navController.navigate(Discovery)
                                service.startDiscovering(this@MainActivity)
                            },
                            onChatScreenNavigateRequested = {
                                navController.navigate(Chat)
                            },
                            onDeviceConnectRequest = {address->
                                try{
                                    service.connect(address)
                                } catch (e: Exception) {
                                    navController.navigate(Error)
                                }
                            },
                            onServerSocketOpenRequested = {
                                service.start()
                            }
                        )
                    }
                    composable<Chat>{
                        ChatScreen(modifier=Modifier.fillMaxSize())
                    }
                    dialog<Error>{
                        ErrorDialog {
                            navController.popBackStack()
                        }
                    }
                    dialog<Discovery>{
                        ConnectableDeviceListDialog(
                            deviceList = discoveredDevice.value.toList(),
                            bluetoothDiscoveringState = bluetoothState.value,
                            onSelectDevice = {
                                service.connect(it.address)
                            },
                            onDismiss = {
                                service.finishDiscovering()
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }

    private val bluetoothPermissions = mutableListOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= 31) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        }
    }

    private fun requestBluetoothConnectPermission() {
        val notGrantedPermissionList = mutableListOf<String>()

        for (permission in bluetoothPermissions) {

            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result == PackageManager.PERMISSION_GRANTED) continue
            notGrantedPermissionList.add(permission)
            if (shouldShowRequestPermissionRationale(permission)) explainBluetoothConnectPermission()

        }
        if (notGrantedPermissionList.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(notGrantedPermissionList.toTypedArray())
        }
    }

    private fun explainBluetoothConnectPermission() {
        Toast.makeText(
            this,
            ContextCompat.getString(this, R.string.permission_required),
            Toast.LENGTH_SHORT
        ).show()
    }
}