package com.mawared.mawaredvansale.controller.settings

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.mawared.mawaredvansale.App
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.utilities.toast
import kotlinx.android.synthetic.main.device_list.*
import print.Print
import java.text.DecimalFormat
import java.util.*

class DeviceListActivity: Activity() {
    val TAG = DeviceListActivity::class.java.simpleName
    private var D = true
    private var mBtAdapter: BluetoothAdapter? = null
    private lateinit var mPairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLED_BLUETOOTH = 1
    private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    var pairedDeviceList: List<String>? = null
    var newDeviceList: List<String>? = null
    var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
    var mNewDevicesArrayAdapter: ArrayAdapter<String>? = null
    var toothAddress: String? = null
    var toothName: String? = null
    private lateinit var ctx: Context
    private var strAddressList = ""
    private var thread: Thread? = null

    internal var handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            progress.setVisibility(View.GONE)
            val intent = Intent()
            intent.putExtra("is_connected", if (msg.what == 0) "OK" else "NO")
            intent.putExtra("BTAddress", toothAddress)
            setResult(Print.ACTIVITY_CONNECT_BT, intent)
            finish()
        }
    }
    private var message: Message? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ctx = applicationContext
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
        setContentView(R.layout.device_list)
        setResult(Activity.RESULT_CANCELED)
        button_scan.setOnClickListener {
            strAddressList = ""
            doDiscovery()
            it.visibility = View.GONE
        }

        // Array Adapter
        mPairedDevicesArrayAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getPairedData())
        mNewDevicesArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        val pairedListView = findViewById<View>(R.id.paired_devices) as ListView
        val newDevicesListView = findViewById<View>(R.id.new_devices) as ListView
        pairedListView.adapter = mPairedDevicesArrayAdapter
        newDevicesListView.adapter = mNewDevicesArrayAdapter
        val ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST"
        val intent = IntentFilter()
        intent.addAction(BluetoothDevice.ACTION_FOUND)// 用BroadcastReceiver来取得搜索结果
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        intent.addAction(ACTION_PAIRING_REQUEST)
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, intent)

        try {
            pairedListView.onItemClickListener = mDeviceClickListener
            newDevicesListView.onItemClickListener = mDeviceClickListener
        }catch (e: Exception){
            toast(ctx.getString(R.string.activity_devicelist_get_device_err)+e)
        }
    }

    private fun getPairedData(): List<String>{
        val data: ArrayList<String> = arrayListOf()
        //默认的蓝牙适配器
        mBtAdapter = BluetoothAdapter.getDefaultAdapter()
        // 得到当前的一个已经配对的蓝牙设备
        val pairedDevices = mBtAdapter!!.getBondedDevices()
        val pairedListView = findViewById<View>(R.id.paired_devices) as ListView
        val newDevicesListView = findViewById<View>(R.id.new_devices) as ListView

        if (pairedDevices.size > 0) {
            findViewById<View>(R.id.title_paired_devices).visibility = View.VISIBLE
            for (device in pairedDevices)
            //遍历
            {
                data.add(device.name + "\n" + device.address)
            }
        } else {
            val noDevices = resources.getText(R.string.activity_devicelist_none_paired).toString()
            data.add(noDevices)
        }

        return data
    }

    override fun onDestroy() {
        super.onDestroy()
        // 确认是否还需要做扫描
        if(mBtAdapter != null){
            mBtAdapter!!.cancelDiscovery()
            mBtAdapter = null
        }
        if(thread != null){
            val dummy: Thread = thread!!
            thread = null
            dummy.interrupt()
        }
    }

    /**
     * 启动装置发现的BluetoothAdapter
     */
    fun doDiscovery(){
        if (D) Log.d(TAG, "doDiscovery()")
        // 在标题中注明扫描
        setProgressBarIndeterminateVisibility(true)
        setTitle(R.string.activity_devicelist_scanning)
        // 打开子标题的新设备
        findViewById<View>(R.id.title_new_devices).visibility = View.VISIBLE
        // 若启动了扫描，关闭扫描
        if (mBtAdapter!!.isDiscovering()) {
            mBtAdapter!!.cancelDiscovery()
        }
        //扫描
        var intStartCount = 0
        while (!mBtAdapter!!.startDiscovery() && intStartCount < 5) {
            Log.e("BlueTooth", "扫描尝试失败")
            intStartCount++
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

    // 给列表的中的蓝牙设备创建监听事件
    var mDeviceClickListener: AdapterView.OnItemClickListener =
        AdapterView.OnItemClickListener { av, v, arg2, arg3 ->
            val hasConnected = false
            progress.visibility = View.VISIBLE
            try {
                if (mBtAdapter!!.isDiscovering()) {
                    mBtAdapter!!.cancelDiscovery()
                }

                //取得蓝牙mvc地址
                val info = (v as TextView).text.toString()
                toothAddress = info.substring(info.length - 17)
                if (!toothAddress!!.contains(":")) {
                    return@OnItemClickListener
                }
                thread = Thread(Runnable {
                    // TODO Auto-generated method stub
                    try {
                        //                            Config.isLog=true;
                        //val portOpen = Print.PortOpen(ctx, "Bluetooth,$toothAddress")
                        App.prefs.bluetooth_port = "Bluetooth,$toothAddress"
                        App.prefs.printer_name = info.subSequence(0, info.length-18).toString()

                        message = Message()
                        message!!.what = 1// portOpen
                        handler.sendMessage(message)
                    } catch (e: Exception) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                })
                thread!!.start()

            } catch (e: Exception) {
                progress.visibility = View.GONE
                e.printStackTrace()
            }
        }

    // 扫描完成时候，改变按钮text
    val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action
            var device: BluetoothDevice? = null
            // 搜索设备时，取得设备的MAC地址
            if (BluetoothDevice.ACTION_FOUND == action) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if (device!!.bondState == BluetoothDevice.BOND_NONE) {
                    if (device.bluetoothClass.majorDeviceClass == 1536) {
                        if (!strAddressList.contains(device.address)) {
                            val b = intent.extras
                            val mobject = b!!.get("android.bluetooth.device.extra.RSSI").toString()
                            val valueOf = Integer.valueOf(mobject)
                            val power = ((Math.abs(valueOf) - 59) / (10 * 2.0)).toFloat()
                            val pow = Math.pow(10.0, power.toDouble()).toFloat()
                            strAddressList += device.address + ","
                            val decimalFormat = DecimalFormat("0.00")
                            mNewDevicesArrayAdapter!!.add(
                                device.name + "  " + decimalFormat.format(
                                    pow.toDouble()
                                ) + " m" + "\n" + device.address
                            )
                        }
                    }
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED == action) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                when (device!!.bondState) {
                    BluetoothDevice.BOND_BONDING -> Log.d("BlueToothTestActivity", "正在配对......")
                    BluetoothDevice.BOND_BONDED -> Log.d("BlueToothTestActivity", "完成配对")
                    BluetoothDevice.BOND_NONE -> Log.d("BlueToothTestActivity", "取消配对")
                    else -> {
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                setProgressBarIndeterminateVisibility(false)
                setTitle(R.string.activity_devicelist_select_device)
                if (mNewDevicesArrayAdapter!!.getCount() == 0) {
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (thread != null) {
            val dummy = thread
            thread = null
            dummy!!.interrupt()
        }
    }
}