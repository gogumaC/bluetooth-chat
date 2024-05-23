package kr.co.teamfresh.kyb.bluetoothchat.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.BluetoothChatService
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.MESSAGE_READ
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.MESSAGE_TOAST
import kr.co.teamfresh.kyb.bluetoothchat.bluetooth.MESSAGE_WRITE
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var bluetoothEnableLauncher: ActivityResultLauncher<Intent>
    private lateinit var bluetoothSettingLauncher: ActivityResultLauncher<Intent>
    private lateinit var bluetoothScanLauncher: ActivityResultLauncher<Intent>

    private val bluetoothPermissions = mutableListOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).apply{
        if(Build.VERSION.SDK_INT==31){
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        }
    }


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
                    map["DENIED"]?.let {}
                    map["EXPLAINED"]?.let {}
                } else {

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

        val service = BluetoothChatService(mHandler, this, bluetoothAdapter!!)

        //블루투스가 활성화 되어있는지 확인
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT) -- deprecated
            bluetoothEnableLauncher.launch(enableBtIntent)
        }

        service.getPairedDeviceList()
        setContent {
            BluetoothChatTheme {
                // A surface container using the 'background' color from the theme
                ConnectScreen(
                    modifier=Modifier.fillMaxSize(),
                    service = service,
                    onBluetoothDeviceScanRequest = {
                        Log.d("checkfor","discovering state : ${bluetoothAdapter.isDiscovering} findStart!")
                        if(!bluetoothAdapter.isDiscovering) {
                            val res = bluetoothAdapter.startDiscovery()
                            Log.d("checkfor","start res: $res")
                        }
                    }

                )
            }
        }
    }

    private fun requestBluetoothConnectPermission() {
        val permissionList = mutableListOf<String>()

        for (permission in bluetoothPermissions) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(permission)
            }
        }

        if (permissionList.isNotEmpty()) {
            bluetoothPermissionLauncher.launch(permissionList.toTypedArray())
        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ContextCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) == PackageManager.PERMISSION_DENIED
//            ) {
//                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
//            }
//        }

    }
}