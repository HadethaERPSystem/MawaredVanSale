package com.mawared.mawaredvansale.domain

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

//class FoundDeviceReceiver (
//    private val onDeviceFound : (BluetoothDevice) -> Unit
//): BroadcastReceiver(){
//    override fun onReceive(context: Context?, intent: Intent?) {
//       when(intent?.action){
//           BluetoothDevice.ACTION_FOUND ->{
//               val device = intent.getParcelableExtra( BluetoothDevice.EXTRA_NAME )
//           }
//       }
//    }
//
//}