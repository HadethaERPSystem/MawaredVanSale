package com.mawared.mawaredvansale.interfaces

import android.view.View

interface IAddNavigator<T> {
    fun onDelete(baseEo: T)
    fun onShowDatePicker(v: View)
    fun clear(code: String)
}
