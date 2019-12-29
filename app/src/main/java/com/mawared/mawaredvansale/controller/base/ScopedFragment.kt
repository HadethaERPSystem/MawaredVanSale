package com.mawared.mawaredvansale.controller.base

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

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

    fun <T> showDialog(context: Context, title: String, msg: String, baseEo: T, doRun: (obj: T) -> Unit) {
        AlertDialog.Builder(context).apply {
            setTitle(title)
            setMessage(msg)
            setPositiveButton("OK") { _, _ ->
                doRun(baseEo)
            }

            setNegativeButton("Cancel") { _, _ ->
                //pass
            }
        }.create().show()
    }

    fun hideKeyboard(){
//        val imm =
//            activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view!!.windowToken, 0)

        val inputManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        if(inputManager.isAcceptingText){
            inputManager.hideSoftInputFromWindow(view!!.windowToken, 0)
        }
    }
}