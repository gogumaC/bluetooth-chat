package kr.co.teamfresh.kyb.bluetoothchat

import android.Manifest
import android.app.Activity
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

    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var bluetoothEnableLauncher: ActivityResultLauncher<Intent>
    private lateinit var bluetoothSettingLauncher: ActivityResultLauncher<Intent>
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
                if (result.resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
                } else if (result.resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "bluetooth not enable", Toast.LENGTH_SHORT).show()
                }

            }

        bluetoothPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) Toast.makeText(
                    this,
                    "bluetooth connect permission granted",
                    Toast.LENGTH_SHORT
                ).show()
                else Toast.makeText(this, "bluetooth connect permission denied", Toast.LENGTH_SHORT)
                    .show()

            }

        bluetoothSettingLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            }

        //블루투스가 기기에서 지원되는지 확인
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
        }

        //블루투스 권한 확인
        requestBluetoothConnectPermission()

        val service = BluetoothChatService(mHandler, this, bluetoothAdapter!!)

        //블루투스가 활성화 되어있는지 확인
        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT) -- deprecated
            bluetoothEnableLauncher.launch(enableBtIntent)
        }


        val bluetoothSettingIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        service.getPairedDeviceList()
        setContent {
            BluetoothChatTheme {
                // A surface container using the 'background' color from the theme
                ChatScreen(onClickPlusButton = {
                    bluetoothSettingLauncher.launch(
                        bluetoothSettingIntent
                    )
                }, Modifier.fillMaxSize(), service)
            }
        }
    }

    private fun requestBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_DENIED
            ) {
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

    }
}