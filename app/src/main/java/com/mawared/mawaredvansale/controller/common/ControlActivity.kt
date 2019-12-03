package com.mawared.mawaredvansale.controller.common

import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.settings.DeviceListActivity
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.control_layout.*
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.net.URL
import java.util.*

class ControlActivity: AppCompatActivity(), Runnable {

    companion object{
        val mUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var mBluetoothSocket: BluetoothSocket? = null
        lateinit var mProgress: ProgressDialog
        var mBluetoothAdapter: BluetoothAdapter? = null
        var m_isConnected: Boolean = false
        lateinit var m_address: String
        val REQUEST_ENABLE_BT = 2
        val REQUEST_CONNECT_DEVICE = 1
        private lateinit var mBluetoothDevice: BluetoothDevice
        const val EXTRA_DATA_TICKETS = "tickets"
        private lateinit var outputStream: OutputStream
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        val mScan = findViewById<Button>(R.id.mScan)

        mScan.setOnClickListener {
            if(m_isConnected){
                disconnect()
            }else{
                connect()
            }
        }

        val mPrint = findViewById<Button>(R.id.mPrint)
        mPrint.setOnClickListener {
            if(m_isConnected){
                val bundle = intent.extras
                if(bundle != null){
                    val lines = bundle.getSerializable(EXTRA_DATA_TICKETS)
                    // val lines = bundle?.get(EXTRA_DATA_TICKETS)
                    if(lines != null)
                        print(lines as List<Ticket>)
                }

            }
        }
    }

    private fun connect(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(mBluetoothAdapter == null){
            control_layout_lc.snackbar("")
        }else{
            if(!mBluetoothAdapter!!.isEnabled){
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }else{
                val connectIntent = Intent(this@ControlActivity, DeviceListActivity::class.java)
                startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE)
            }
        }
    }

    private fun disconnect(){
        if(mBluetoothSocket != null){
            try {
                mBluetoothSocket!!.close()
                mBluetoothSocket = null
                m_isConnected = false
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CONNECT_DEVICE ->{
                if(resultCode == Activity.RESULT_OK){
                    val mExtra = data!!.extras
                    val mDeviceAddress: String? = mExtra?.getString("Device_address")
                    mBluetoothDevice = mBluetoothAdapter!!.getRemoteDevice(mDeviceAddress)
                    mProgress = ProgressDialog.show(this, getString(R.string.connecting), mBluetoothDevice.name + " : " + mBluetoothDevice.address, true, false)
                    val thread = Thread(this)
                    thread.start()
                }
            }
            REQUEST_ENABLE_BT ->{
                if(resultCode == Activity.RESULT_OK){
                    val connectIntent = Intent(this, DeviceListActivity::class.java)
                    startActivityForResult(connectIntent, REQUEST_CONNECT_DEVICE)
                }else{
                    control_layout_lc.snackbar(getString(R.string.msg_notify_bluetooth_error))
                }
            }
        }
    }


    override fun run(){
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(mUUID)
            mBluetoothAdapter!!.cancelDiscovery()
            mBluetoothSocket!!.connect()
            mHandler.sendEmptyMessage(0)
        }catch (e: IOException){

        }
    }

    private fun closeSocket(nOpenSocket: BluetoothSocket){
        try {
            nOpenSocket.close()
            Log.d("TAG", "socketClosed")
        }catch (e: IOException){
            Log.d("TAG", "CouldNotCloseSocket")
        }
    }

    private val mHandler = object: Handler(){
       override fun handleMessage(msg: Message){
            mProgress.dismiss()
            m_isConnected = true
        }
    }


    // Printing
    private fun print(lines: List<Ticket>) {
        val t = object:Thread() {
            override fun run() {
                try {
                    outputStream = mBluetoothSocket!!.outputStream
                    for (l: Ticket in lines) {
                        when(l.align){
                            AlignText.LEFT -> {
                                outputStream.write(PrinterCommands.ESC_ALIGN_LEFT)
                            }
                            AlignText.RIGHT -> {
                                outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT)
                            }
                            AlignText.CENTER -> {
                                outputStream.write(PrinterCommands.ESC_ALIGN_CENTER)
                            }
                        }
                        when (l.type) {
                            LineType.Text -> {
                                outputStream.write(l.text!!.toByteArray())
                            }
                            LineType.Image -> {
                                try {
//                                    val theBitmap = Glide.with(this@ControlActivity).
//                                        load(l.text).asBitmap().
//                                        into(100, 100).
//                                        get()
                                    //val command = BitmapFactory.de
                                    val url = URL(l.text)
                                    val bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                                    val b = BitmapUtils.convertBitmapToByteArray(bmp)
                                    outputStream.write(b)
                                }catch (e:Exception){
                                    e.printStackTrace()
                                }
                            }
                        }

                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }

        }
        t.start()
    }


}