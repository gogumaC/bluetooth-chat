package com.gogumac.bluetoothchat.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.gogumac.bluetoothchat.R
import com.gogumac.bluetoothchat.bluetooth.BluetoothService
import com.gogumac.bluetoothchat.bluetooth.BluetoothState
import com.gogumac.bluetoothchat.ui.dialogs.ConnectableDeviceListDialog
import com.gogumac.bluetoothchat.ui.dialogs.DisconnectAlertDialog
import com.gogumac.bluetoothchat.ui.dialogs.ErrorDialog
import com.gogumac.bluetoothchat.ui.dialogs.LoadingDialog
import com.gogumac.bluetoothchat.ui.theme.BluetoothChatTheme
import com.gogumac.bluetoothchat.ui.viewmodel.ChatScreenViewModel

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var bluetoothEnableLauncher: ActivityResultLauncher<Intent>
    private lateinit var bluetoothSettingLauncher: ActivityResultLauncher<Intent>
    private lateinit var bluetoothScanLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        //블루투스가 기기에서 지원되는지 확인
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show()
        }

        //블루투스 권한 확인
        requestBluetoothConnectPermission()

        val service = BluetoothService(bluetoothAdapter!!, this)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val discoveredDevice = service.discoveredDevices.collectAsState()
            val bluetoothState = service.state.collectAsState()
            val savedBluetoothDevices = service.pairedDeviceList.collectAsState()
            val chatScreenViewModel = ChatScreenViewModel(service)

            if(bluetoothState.value== BluetoothState.STATE_CLOSE_CONNECT){
                navController.popBackStack<Connect>(inclusive = false)
                Toast.makeText(LocalContext.current, R.string.disconnected, Toast.LENGTH_SHORT).show()
            }
            BluetoothChatTheme {
                NavHost(navController = navController, startDestination = Connect,modifier=Modifier.safeDrawingPadding()) {
                    composable<Connect> {
                        ConnectScreen(
                            modifier = Modifier.fillMaxSize(),
                            deviceList = savedBluetoothDevices.value,
                            onBluetoothDeviceScanRequest = {
                                navController.navigate(Discovery)
                                service.startDiscovering(this@MainActivity)
                            },
                            onChatScreenNavigateRequested = {
                                navController.navigate(Chat)
                            },
                            onDeviceConnectRequest = { address ->
                                navController.navigate(DialogConnectLoading(address))
                            },
                            onServerSocketOpenRequested = {
                                navController.navigate(ServerSocketLoading)
                                CoroutineScope(Dispatchers.Main).launch {
                                    val res = async { service.openServerSocket() }.await()
                                    navController.popBackStack()
                                    if (res) {
                                        navController.navigate(Chat)
                                    } else {
                                        navController.navigate(Error)
                                    }
                                }
                            },
                            onSetDiscoverableRequest = {
                                service.setBluetoothDiscoverable()
                            }
                        )
                    }
                    composable<Chat> {
                        ChatScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = chatScreenViewModel,
                            onBackPressed = {
                                navController.navigate(DisconnectAlertDialog)
                            })
                    }
                    dialog<Error> {
                        ErrorDialog {
                            navController.popBackStack()
                        }
                    }
                    dialog<Discovery> {
                        ConnectableDeviceListDialog(
                            deviceList = discoveredDevice.value.toList(),
                            bluetoothDiscoveringState = bluetoothState.value,
                            onSelectDevice = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    service.requestPairing(it.address).collect { state ->
                                        withContext(Dispatchers.Main) {
                                            when (state) {
                                                BluetoothState.STATE_BONDING -> {
                                                    navController.navigate(DialogPairingLoading)
                                                }

                                                BluetoothState.STATE_BONDED -> {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        ContextCompat.getString(
                                                            this@MainActivity,
                                                            R.string.complete_pairing
                                                        ),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    navController.popBackStack<Connect>(inclusive = false)
                                                }

                                                BluetoothState.STATE_NONE -> {
                                                    navController.popBackStack()
                                                    navController.navigate(Error)
                                                }

                                                else -> {}
                                            }
                                        }
                                    }
                                }
                            },
                            onDismiss = {
                                service.finishDiscovering()
                                navController.popBackStack()
                            }
                        )
                    }
                    dialog<ServerSocketLoading> {
                        val toastMsg = stringResource(id = R.string.close_serever_socket_noti)
                        LoadingDialog(
                            modifier = Modifier,
                            onDismissRequest = {
                                navController.popBackStack()
                                service.closeServerSocket()
                                Toast.makeText(this@MainActivity, toastMsg, Toast.LENGTH_SHORT)
                                    .show()
                            },
                            text = stringResource(id = R.string.open_server_socket)
                        )

                    }
                    dialog<DialogConnectLoading> {backStackEntry->
                        val address=backStackEntry.toRoute<DialogConnectLoading>().deviceAddress

                        val job=CoroutineScope(Dispatchers.Main).launch(start=CoroutineStart.LAZY) {
                            val res = async { service.requestConnect(address) }.await()
                            navController.popBackStack()
                            if (res) {
                                Toast.makeText(
                                    this@MainActivity,
                                    ContextCompat.getString(this@MainActivity,R.string.connected),
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(Chat)
                            } else {
                                navController.navigate(Error)
                            }
                        }
                        LoadingDialog(
                            modifier = Modifier,
                            onDismissRequest = {
                                job.cancel()
                                navController.popBackStack()
                            },
                            text = stringResource(
                                id = R.string.connecting
                            )
                        )
                        job.start()
                    }

                    dialog<DialogPairingLoading> {
                        LoadingDialog(
                            modifier = Modifier,
                            onDismissRequest = { },
                            text = stringResource(
                                id = R.string.request_paring_alert
                            )
                        )
                    }

                    dialog<DisconnectAlertDialog> {
                        DisconnectAlertDialog(
                            onConfirmed = {
                                val res = service.finishConnect()
                                if (res) {
                                    //navController.popBackStack<Chat>(inclusive = true)
                                    Toast.makeText(
                                        this@MainActivity,
                                        ContextCompat.getString(
                                            this@MainActivity,
                                            R.string.disconnected
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        ContextCompat.getString(
                                            this@MainActivity,
                                            R.string.error_occurred
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onCanceled = {
                                navController.popBackStack()
                            })
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