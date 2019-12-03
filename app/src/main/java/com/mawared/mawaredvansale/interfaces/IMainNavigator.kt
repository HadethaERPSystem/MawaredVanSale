package com.mawared.mawaredvansale.interfaces

interface IMainNavigator<T> {
    fun onItemDeleteClick(baseEo: T)
    fun onItemEditClick(baseEo: T)
    fun onItemViewClick(baseEo: T)
}