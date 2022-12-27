package com.mawared.mawaredvansale.utilities

import com.mawared.mawaredvansale.data.db.entities.security.Menu
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mawared.mawaredvansale.App
import java.lang.reflect.Type


object MenuSysPrefs {
    fun saveMenu(menus: List<Menu>){
        val gson = Gson()
        val json = gson.toJson(menus)
        App.prefs.menuPref = json
    }

    fun getMenu() : List<Menu>{
        var arrMenu : List<Menu> = arrayListOf()
        val menuString = App.prefs.menuPref
        if(!menuString.isNullOrEmpty()){
            val gson = Gson()
            val type: Type = object : TypeToken<List<Menu?>?>() {}.getType()
            arrMenu = gson.fromJson(menuString, type)
        }

        return arrMenu
    }

    fun getPermission(menu_code: String): String{
        val menus = getMenu()
        var perm : String? = null
        val menu = menus.find{it.menu_code == menu_code}
        if(menu != null){
            perm = menu.permission
        }

        return perm ?: ""
    }
}