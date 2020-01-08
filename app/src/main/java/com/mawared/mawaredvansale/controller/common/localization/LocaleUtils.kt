package com.mawared.mawaredvansale.controller.common.localization

import android.content.Context
import android.content.res.Configuration
import com.mawared.mawaredvansale.App
import java.util.*

class LocaleManager {
    fun setLocale(c: Context) {
        setNewLocale(c, getLanguage(c))
    }

    fun setNewLocale(c: Context, language: String) {
        persistLanguage(c, language)
        updateResources(c, language)
    }

    fun getLanguage(c: Context): String { return App.prefs.systemLanguage!! }

    private fun persistLanguage(c: Context, language: String) {  }

    private fun updateResources(context: Context, language: String) {
        val locale = Locale(language);
        Locale.setDefault(locale);

        val res = context.resources
        val config = Configuration(res.getConfiguration());
        config.locale = locale
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}