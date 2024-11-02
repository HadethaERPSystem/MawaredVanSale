package com.mawared.mawaredvansale.controller.common.printing

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.mawared.mawaredvansale.App
import java.io.IOException
import java.util.*

//class BluetoothPrinter(private val context: Context, private val device: BluetoothDevice) {
//
//    var bluetoothAdapter: BluetoothAdapter? = null
//    private var socket: BluetoothSocket? = null
//
//    @Throws(IOException::class)
//    fun connect(): BluetoothSocket? {
//        try {
//            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//            val device = bluetoothAdapter.getRemoteDevice("DC:0D:30:63:34:1A")
//
//            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//
//            }
//            socket = device.createRfcommSocketToServiceRecord(uuid)
//            socket?.connect()
//        }
//        catch (e: Exception){
//            e.printStackTrace()
//        }
//        return socket
//    }
//
//    @Throws(IOException::class)
//    fun print(data: ByteArray) {
//        socket?.outputStream?.write(data)
//    }
//
//    @Throws(IOException::class)
//    fun close() {
//        socket?.close()
//    }
//}

class BluetoothPrinter(private val context: Context) {

    private var socket: BluetoothSocket? = null

    @Throws(IOException::class)
    fun connect(): BluetoothSocket? {
        try {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val device = bluetoothAdapter.getRemoteDevice(App.prefs.bluetooth_address)

            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {

            }
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket?.connect()
        }
        catch (e: Exception){
            e.printStackTrace()
        }
        return socket
    }

    @Throws(IOException::class)
    fun print(data: ByteArray) {
        socket?.outputStream?.write(data)
    }

    @Throws(IOException::class)
    fun close() {
        socket?.close()
    }
}