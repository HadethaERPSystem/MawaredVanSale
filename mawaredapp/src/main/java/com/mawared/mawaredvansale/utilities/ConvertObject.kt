package com.mawared.mawaredvansale.utilities

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

object ConvertObject {
    val gson = Gson()

    //convert a data class to a map
    fun <T> T.serializeToMap(): Map<String, Any> {
        return convert()
    }

    //convert a map to a data class
    inline fun <reified T> Map<String, Any>.toDataClass(): T {
        return convert()
    }

    //convert an object of type I to type O
    inline fun <I, reified O> I.convert(): O {
        val json = gson.toJson(this)
        return gson.fromJson(json, object : TypeToken<O>() {}.type)
    }
   //org.jetbrains.kotlin:kotlin-stdlib:1.4.31

    fun <T : Any> toMap(obj: T): Map<String, Any?> {
        return (obj::class as KClass<T>).memberProperties.associate { prop ->
            prop.name to prop.get(obj)?.let { value ->
                if (value::class.isData) {
                    toMap(value)
                } else {
                    value
                }
            }
        }
    }

    fun <T : Any> toMap(obj: List<T>) : ArrayList<Map<String, Any?>>{
        val dics: ArrayList<Map<String, Any?>> = ArrayList()
        for (o in obj){
            val dic = o.serializeToMap()
            dics.add(dic)
        }
        return  dics
    }
}