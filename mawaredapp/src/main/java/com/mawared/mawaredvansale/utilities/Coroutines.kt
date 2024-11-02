package com.mawared.mawaredvansale.utilities

import kotlinx.coroutines.*

object Coroutines {

    fun main(work: suspend (()-> Unit)) =
        CoroutineScope(Dispatchers.Main).launch {
            work()
        }

    fun io(work: suspend (()-> Unit)) =
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }

    fun<T: Any> ioThenMain(work: suspend (()-> T?), callback:((T?)->Unit)) =
        CoroutineScope(Dispatchers.Main).launch {
            val data = CoroutineScope(Dispatchers.IO).async  rt@{
                return@rt work()
            }.await()
            callback(data)
        }
}