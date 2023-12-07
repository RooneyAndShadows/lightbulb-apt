package com.github.rooneyandshadows.lightbulb.apt.android.core.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.PreferenceManager
import com.github.rooneyandshadows.java.commons.string.StringUtils
import java.util.*

@Suppress("unused")
class PreferenceUtils {
    companion object {
        @SuppressLint("ApplySharedPref")
        @JvmStatic
        fun clearKey(
            context: Context,
            preferenceKey: String,
        ): Companion {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()
            editor.remove(preferenceKey)
            editor.commit()
            return Companion
        }

        @SuppressLint("ApplySharedPref")
        @JvmStatic
        fun saveString(
            context: Context,
            preferenceKey: String,
            preferenceValue: String
        ): Companion {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()
            editor.putString(preferenceKey, preferenceValue)
            editor.commit()
            return Companion
        }

        @SuppressLint("ApplySharedPref")
        @JvmStatic
        fun saveBoolean(
            context: Context,
            preferenceKey: String,
            preferenceValue: Boolean
        ): Companion {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()
            editor.putBoolean(preferenceKey, preferenceValue)
            editor.commit()
            return Companion
        }

        @JvmStatic
        fun saveUUID(
            context: Context,
            preferenceKey: String,
            preferenceValue: UUID
        ): Companion {
            return saveString(context, preferenceKey, preferenceValue.toString())
        }

        @SuppressLint("ApplySharedPref")
        @JvmStatic
        fun saveInt(
            context: Context,
            preferenceKey: String,
            preferenceValue: Int
        ): Companion {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = preferences.edit()
            editor.putInt(preferenceKey, preferenceValue)
            editor.commit()
            return Companion
        }

        @JvmStatic
        fun getString(context: Context, preferenceKey: String, defaultValue: String): String {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getString(preferenceKey, defaultValue)!!
        }

        @JvmStatic
        fun getBoolean(context: Context, preferenceKey: String, defaultValue: Boolean): Boolean {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getBoolean(preferenceKey, defaultValue)
        }

        @JvmStatic
        fun getUUID(context: Context, preferenceKey: String): UUID? {
            val savedValue = getString(context, preferenceKey, "")
            return if (StringUtils.isNullOrEmptyString(savedValue)) null
            else UUID.fromString(savedValue)
        }

        @JvmStatic
        fun getInt(context: Context, preferenceKey: String, defaultValue: Int): Int {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getInt(preferenceKey, defaultValue)
        }
    }
}