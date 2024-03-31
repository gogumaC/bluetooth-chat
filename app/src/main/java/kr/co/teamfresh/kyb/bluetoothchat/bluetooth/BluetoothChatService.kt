package kr.co.teamfresh.kyb.bluetoothchat.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
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
    private var mAcceptThread: AcceptThread? = null

    private val myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")//"8CE255C0-200A-11E0-AC64-0800200C9A66")
    private val NAME="BluetoothChat"

    init{
        start()
    }

    fun start() {

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
            mAcceptThread= AcceptThread().apply{start()}
        }

    }

    //@SuppressLint("MissingPermission")
    fun connect(deviceAddress: String) {
        Log.d(TAG, "connect to: " + deviceAddress);

        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        //if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()


        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
        try{
            mConnectThread = ConnectThread(myUUID, device).apply {
                start()
            }
        }catch(e:Exception){
            Log.d(TAG,"connect fail : $e")

        }

    }

    fun connected(socket: BluetoothSocket, device: BluetoothDevice) {

        Log.d(TAG,"connected $device")

        if (mConnectThread != null) {
            mConnectThread!!.cancel()
            mConnectThread = null
        }
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }
        if(mAcceptThread!=null){
            mAcceptThread!!.cancel()
            mAcceptThread=null
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
        if(mAcceptThread!=null){
            mAcceptThread!!.cancel()
            mAcceptThread=null
        }
    }


    @SuppressLint("MissingPermission") // 이거는 앞에서 미리 검사할건데 나중에 합치는게 좋을듯
    fun getPairedDeviceList(): List<BluetoothDevice> {

        return bluetoothAdapter.bondedDevices.toList()

    }


    private inner class AcceptThread : Thread() {
        private val mmServerSocket: BluetoothServerSocket? by lazy(LazyThreadSafetyMode.NONE) {
            bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, myUUID)
        }


        override fun run() {

            name="AcceptThread"
            Log.d(TAG, "Socket's accept() method start")
            var shouldLoop = true
            while (shouldLoop) {
                val socket: BluetoothSocket? = try {
                    val a=mmServerSocket
                    mmServerSocket?.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "Socket's accept() method failed", e)
                    shouldLoop = false
                    null
                }

                socket?.also {
                    //manageMyConnectedSocket(it)
                    connected(it,it.remoteDevice)
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
//            val m = device.javaClass.getMethod(
//                "createInsecureRfcommSocketToServiceRecord", *arrayOf<Class<*>>(
//                    UUID::class.java
//                )
//            )
//            val uuid=device.uuids.toList()[0].uuid
//            m.invoke(device,uuid) as BluetoothSocket
            device.createInsecureRfcommSocketToServiceRecord(myUUID)
        }


        override fun run() {
            Log.d(TAG, "Begin ConnectThread ${device.bondState==BluetoothDevice.BOND_BONDED} | $myUUID")

            Log.d(TAG,"Try connect : ")
            val a=connectSocket?.isConnected
//                val clazz=connectSocket!!.remoteDevice.javaClass
//                val paramTypes= arrayOf<Class<*>>(Integer.TYPE)
//                val m=clazz.getMethod("createRfcommSocket",*paramTypes)
//                val fallbackSocket=m.invoke(connectSocket!!.remoteDevice,Integer.valueOf(1)) as BluetoothSocket
//            fallbackSocket.connect()

            connectSocket?.connect()
            Log.d(TAG,"Connect success")
//                connectSocket?.let {
//                    val connectedThread = ConnectedThread(it)
//                }

//            try {
//                Log.d(TAG,"Try connect : ")
////                val clazz=connectSocket!!.remoteDevice.javaClass
////                val paramTypes= arrayOf<Class<*>>(Integer.TYPE)
////                val m=clazz.getMethod("createRfcommSocket",*paramTypes)
////                val fallbackSocket=m.invoke(connectSocket!!.remoteDevice,Integer.valueOf(1)) as BluetoothSocket
////                fallbackSocket.connect()
//                connectSocket?.connect()
//                Log.d(TAG,"Connect success")
////                connectSocket?.let {
////                    val connectedThread = ConnectedThread(it)
////                }
//            } catch (e: IOException) {
//                connectSocket?.close()
//                Log.d(TAG,"Connect failed and Socket closed")
//                throw Exception("connect fail"+e)
//            }

            //connectSocket?.let { connected(connectSocket!!, device) }
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