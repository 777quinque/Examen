package com.ekzamen

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.util.*

object LocaleHelper {

    private const val TAG = "LocaleHelper"
    private const val LANGUAGE_KEY = "language"

    fun setLocale(context: Context, language: String) {
        Log.d(TAG, "Setting locale to $language")

        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            resources.updateConfiguration(configuration, resources.displayMetrics)
        } else {
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
        }

        val editor: SharedPreferences.Editor =
            context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
        editor.putString(LANGUAGE_KEY, language)
        editor.apply()
    }

    fun getLocale(context: Context): Locale {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString(LANGUAGE_KEY, "ru") ?: "ru"
        Log.d(TAG, "Current locale: $language")
        return Locale(language)
    }
}
