package kr.co.teamfresh.kyb.bluetoothchat

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import kr.co.teamfresh.kyb.bluetoothchat.ui.theme.BluetoothChatTheme

class MainActivity : ComponentActivity() {



    private lateinit var bluetoothPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var bluetoothEnableLauncher:ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothManager:BluetoothManager=getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter:BluetoothAdapter?=bluetoothManager.adapter

        bluetoothEnableLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode== Activity.RESULT_OK){
                Toast.makeText(this,"Bluetooth enabled",Toast.LENGTH_SHORT).show()
            }else if(result.resultCode==Activity.RESULT_CANCELED){
                Toast.makeText(this,"bluetooth not enable",Toast.LENGTH_SHORT).show()
            }

        }

        bluetoothPermissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){isGranted:Boolean->
            if(isGranted) Toast.makeText(this,"bluetooth connect permission granted",Toast.LENGTH_SHORT).show()
            else Toast.makeText(this,"bluetooth connect permission denied",Toast.LENGTH_SHORT).show()

        }

        //블루투스가 기기에서 지원되는지 확인
        if(bluetoothAdapter==null){
            Toast.makeText(this,"Device doesn't support Bluetooth",Toast.LENGTH_SHORT).show()
        }

        //블루투스 권한 확인
        requestBluetoothConnectPermission()

        //블루투스가 활성화 되어있는지 확인
        if(bluetoothAdapter?.isEnabled==false){
            val enableBtIntent= Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT) -- deprecated


            bluetoothEnableLauncher.launch(enableBtIntent)
        }
        setContent {
            BluetoothChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    private fun requestBluetoothConnectPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_DENIED){
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BluetoothChatTheme {
        Greeting("Android")
    }
}