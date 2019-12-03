package com.mawared.mawaredvansale.controller.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import com.mawared.mawaredvansale.App

import com.mawared.mawaredvansale.R
import com.mawared.mawaredvansale.controller.common.getLanguageEncode
import com.tbruyelle.rxpermissions.RxPermissions
import kotlinx.android.synthetic.main.settings_fragment.*

import print.Print
import print.PublicFunction
import java.util.HashMap
import rx.functions.Action1

class SettingsFragment : Fragment() {

    private var ctx: Context? = context
    var arrCodepageLatin: ArrayAdapter<*>? = null
    var arrCodepageAr: ArrayAdapter<*>? = null
    private var PFun: PublicFunction? = null
    private lateinit var et_PrinterPort: EditText
    private lateinit var et_ServerUrl: EditText
    private lateinit var scanBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var spnLatin: Spinner
    private lateinit var spnAr: Spinner

    private lateinit var viewModel: SettingsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.settings_fragment, container, false)
        spnLatin = view.findViewById(R.id.spnCodepageLatin)
        spnAr = view.findViewById(R.id.spnCodepageAr)
        et_PrinterPort = view.findViewById(R.id.et_printer_port)
        et_ServerUrl = view.findViewById(R.id.et_server_url)

        (activity as AppCompatActivity).supportActionBar!!.title = getString(R.string.layout_settings_title)
        (activity as AppCompatActivity).supportActionBar!!.subtitle = ""

        arrCodepageLatin = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item)
        arrCodepageLatin = ArrayAdapter.createFromResource(context!!, R.array.codepage, android.R.layout.simple_spinner_item)
        arrCodepageLatin!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnLatin.adapter = arrCodepageLatin
        spnLatin.onItemSelectedListener = OnItemSelectedCodepageLatin()

        arrCodepageAr = ArrayAdapter<String>(context!!, android.R.layout.simple_spinner_item)
        arrCodepageAr = ArrayAdapter.createFromResource(context!!, R.array.codepage, android.R.layout.simple_spinner_item)
        arrCodepageAr!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnAr.adapter = arrCodepageAr
        spnAr.onItemSelectedListener = OnItemSelectedCodepageAr()


        PFun = PublicFunction(context!!)
        scanBtn = view.findViewById<Button>(R.id.scan_btn)
        scanBtn.setOnClickListener {
            connectionBluetooth()
        }

        saveBtn = view.findViewById<Button>(R.id.btnSaveSettings)
        saveBtn.setOnClickListener {
            saveSettings()
        }
        ctx = activity!!.applicationContext

        iniSetting()

        return view
    }

    private fun saveSettings() {

        App.prefs.server_url = et_ServerUrl.text.toString()
        activity!!.onBackPressed()
    }

    fun iniSetting(){

        try {
            var settingValue: String = App.prefs.lang_Encode_latin ?: "0"
            spnLatin.setSelection(Integer.parseInt(settingValue.split(",")[0]))

            settingValue = App.prefs.lang_Encode_ar ?: "0"
            spnAr.setSelection(Integer.parseInt(settingValue.split(",")[0]))

            et_PrinterPort.setText("${App.prefs.printer_name} : ${App.prefs.bluetooth_port}")
            et_ServerUrl.setText("${App.prefs.server_url}")
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

    private fun connectionBluetooth() {
        val rxPermissions = RxPermissions(activity!!)
        rxPermissions.request(Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION).subscribe(object:Action1<Boolean> {
            override fun call(aBoolean:Boolean) {
                if (aBoolean)
                {
                    val serverIntent = Intent(ctx, DeviceListActivity::class.java)
                    startActivityForResult(serverIntent, Print.ACTIVITY_CONNECT_BT)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            when(resultCode){
                Print.ACTIVITY_CONNECT_BT -> {
                    val txt = activity!!.findViewById<EditText>(R.id.et_printer_port)
                    txt.setText("${App.prefs.printer_name} : ${App.prefs.bluetooth_port}")
                }
            }
        }catch (e: Exception){

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private inner class OnItemSelectedCodepageLatin : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long) {
            try {
                val sCodepage = arrCodepageLatin!!.getItem(arg2)!!.toString()
                val sLEncode = PFun!!.getLanguageEncode(sCodepage)

                val intLanguageNum = PFun!!.getCodePageIndex(sCodepage)

                App.prefs.lang_Encode_latin = "$arg2,"+ sLEncode
                App.prefs.lang_CodePage_latin = intLanguageNum.toString()

            } catch (e: Exception) {

            }
        }

        override fun onNothingSelected(arg0: AdapterView<*>) {
            // TODO Auto-generated method stub
        }
    }

    private inner class OnItemSelectedCodepageAr : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(arg0: AdapterView<*>, arg1: View, arg2: Int, arg3: Long) {
            try {
                val sCodepage = arrCodepageAr!!.getItem(arg2)!!.toString()
                val sLEncode = PFun!!.getLanguageEncode(sCodepage)

                val intLanguageNum = PFun!!.getCodePageIndex(sCodepage)

                App.prefs.lang_Encode_ar = "$arg2,"+ sLEncode
                App.prefs.lang_CodePage_ar = intLanguageNum.toString()

            } catch (e: Exception) {

            }
        }

        override fun onNothingSelected(arg0: AdapterView<*>) {
            // TODO Auto-generated method stub
        }
    }

}