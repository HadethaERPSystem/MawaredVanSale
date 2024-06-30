package com.mawared.mawaredvansale.utilities


import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.constraintlayout.widget.Group
import com.mawared.mawaredvansale.R

// Create extension method

fun Context.toast(message: String){
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

// Prograss bar extension
fun ProgressBar.show(){
    visibility = View.VISIBLE
}

fun ProgressBar.hide(){
    visibility = View.GONE
}

fun Group.show(){
    visibility = View.VISIBLE
}

fun Group.hide(){
    visibility = View.GONE
}

fun Snackbar.allowInfiniteLines(): Snackbar {
    return apply { (view.findViewById<View?>(R.id.snackbar_text) as? TextView?)?.isSingleLine = false }
}
fun View.snackbar(message: String){
    Snackbar.make(this, message, Snackbar.LENGTH_INDEFINITE).also { snackbar ->
        snackbar.setAction("Ok"){
            snackbar.dismiss()
        }
    }.allowInfiniteLines().show()
}

fun View.confirmSnackbar(message: String){
    Snackbar.make(this,
        message,
        Snackbar.LENGTH_INDEFINITE
    ).allowInfiniteLines().also { snackbar ->
        snackbar.setAction("Ok"){
            snackbar.dismiss()
        }
    }
}

fun ContentResolver.getFileName(uri: Uri): String{
    var name = ""
    val cursor = query(uri, null, null, null, null)
    cursor?.use {
        it.moveToFirst()
        name = cursor.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
    }
    return name
}