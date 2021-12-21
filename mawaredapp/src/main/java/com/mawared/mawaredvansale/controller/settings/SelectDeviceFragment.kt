package com.mawared.mawaredvansale.controller.settings


import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.common.ControlActivity
import com.mawared.mawaredvansale.utilities.snackbar
import kotlinx.android.synthetic.main.fragment_select_device.*
import kotlin.collections.ArrayList


class SelectDeviceFragment : Fragment() {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private lateinit var m_pairedDevices: Set<BluetoothDevice>
    private val REQUEST_ENABLED_BLUETOOTH = 1
    //private lateinit var m_bluetoothDevice: BluetoothDevice

    companion object{
        const val EXTRA_ADDRESS: String = "Device_address"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_select_device, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(mBluetoothAdapter == null){
            select_device_cl.snackbar(getString(R.string.msg_notify_device_not_support_bluetooth))
            return
        }
        if(!mBluetoothAdapter!!.isEnabled){
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLED_BLUETOOTH)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        select_device_refresh.setOnClickListener{
            pairedDeviceList()
        }
    }
    private fun pairedDeviceList(){
        m_pairedDevices = mBluetoothAdapter!!.bondedDevices
        val list: ArrayList<BluetoothDevice> = ArrayList()

        if(!m_pairedDevices.isEmpty()){
            for (device: BluetoothDevice in m_pairedDevices){
                list.add(device)
                Log.i("device", "$device")
            }
        } else{
            select_device_cl.snackbar(getString(R.string.msg_notify_no_paired_bluetooth_device))
        }
        val adapter = ArrayAdapter(requireActivity(), R.layout.support_simple_spinner_dropdown_item, list)
        select_device_list.adapter = adapter
        select_device_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            try {
                mBluetoothAdapter!!.cancelDiscovery()
                val device: BluetoothDevice = list[position]
                val address: String = device.address

                val mBundle = Bundle()
                mBundle.putString(EXTRA_ADDRESS, address)
                val mBackIntent = Intent()
                mBackIntent.putExtras(mBundle)

                val intent = Intent(this.context, ControlActivity::class.java)
                intent.putExtra(EXTRA_ADDRESS, address)
                startActivity(intent)
            }catch (e: Exception){

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_ENABLED_BLUETOOTH){
            if(requestCode == Activity.RESULT_OK){
                if(mBluetoothAdapter!!.isEnabled){
                    select_device_cl.snackbar(getString(R.string.msg_notify_bluetooth_enabled))
                }
                else{
                    select_device_cl.snackbar(getString(R.string.msg_notify_bluetooth_disabled))
                }
            }
        } else if(requestCode == Activity.RESULT_CANCELED){
            select_device_cl.snackbar(getString(R.string.msg_notify_bluetooth_enabling_canceled))
        }
    }
}
