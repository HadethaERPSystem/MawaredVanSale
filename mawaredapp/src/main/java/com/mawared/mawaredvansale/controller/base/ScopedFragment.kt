package com.mawared.mawaredvansale.controller.base

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.mawared.mawaredvansale.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.util.*

import kotlin.coroutines.CoroutineContext


abstract class ScopedFragment : Fragment(), CoroutineScope {
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun <T> showDialog(context: Context, title: String, msg: String, baseEo: T, doRun: (obj: T) -> Unit, doCancel:(()->Unit)={}) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(msg)
            setPositiveButton("OK") { _, _ ->
                doRun(baseEo)
            }

            setNegativeButton("Cancel") { _, _ ->
                doCancel()
            }
        }.setCancelable(false).create().show()
    }

    fun hideKeyboard(){

        val inputManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(requireView().windowToken, 0)
        }
    }

    private fun setLocale(lang: String){
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        requireActivity().resources.updateConfiguration(config, requireActivity().resources.displayMetrics)

        App.prefs.systemLanguage = lang

    }

    fun loadLocale(){
        val lang = Locale.getDefault().toString()
        setLocale(lang)
    }
}