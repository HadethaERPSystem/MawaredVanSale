package com.mawared.mawaredvansale.utilities

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.reObserve(owner: LifecycleOwner, observer: Observer<T?>) {
    removeObserver(observer)
    observe(owner, observer)
}