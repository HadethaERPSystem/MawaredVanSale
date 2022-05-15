package com.mawared.mawaredvansale.controller.helpers

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import com.mawared.mawaredvansale.App
import java.util.*

object LocaleHelper {

    const val DEFAULT_LANGUAGE = "ar"
    val LANGUAGES = listOf("en", "ar")

    fun onAttach(context: Context?, defaultLanguage: String): Context? {
        val lang = getPersistedData(defaultLanguage)
        return setLocale(context, lang)
    }

    fun setLocale(context: Context?, language: String): Context? {
        persist(language)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else updateResourcesLegacy(context, language)
    }

    private fun getPersistedData(defaultLanguage: String): String {
        return App.prefs.systemLanguage ?: defaultLanguage
    }

    private fun persist(language: String) {
        App.prefs.systemLanguage = language
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context?, language: String): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context?.resources?.configuration
        configuration?.setLocale(locale)
        configuration?.setLayoutDirection(locale)

        val configure = configuration ?: return context
        return context.createConfigurationContext(configure)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun updateResourcesLegacy(context: Context?, language: String): Context? {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context?.resources
        val configuration = resources?.configuration
        configuration?.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration?.setLayoutDirection(locale)
        }
        @Suppress("DEPRECATION")
        resources?.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

}