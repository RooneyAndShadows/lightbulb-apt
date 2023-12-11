package com.github.rooneyandshadows.lightbulb.apt.android.core.storage

import android.content.Context
import com.github.rooneyandshadows.lightbulb.apt.android.core.utils.PreferenceUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type

abstract class BaseStorage<T : Any> {
    private val dateFormat: String = "yyyy-MM-dd HH:mm:ssZ"
    private val gson: Gson = GsonBuilder().setDateFormat(dateFormat).create()

    abstract fun getStorageClass(): Class<T>

    abstract fun getDefault(): T

    protected fun load(context: Context, key: String): T {
        val value: String = PreferenceUtils.getString(context, key, "")

        return if (value.isBlank()) {
            val default = getDefault()
            PreferenceUtils.saveString(context, key, serializeSettings(default))
            default
        } else {
            deserializeSettings(value, getStorageClass())
        }
    }

    protected fun save(context: Context, settings: T, key: String) {
        PreferenceUtils.saveString(context, key, serializeSettings(settings))
    }

    private fun serializeSettings(settings: T): String {
        return toJson(settings)
    }

    private fun deserializeSettings(settings: String, clazz: Class<T>): T {
        return fromJson(settings, clazz)!!
    }

    private fun toJson(target: T?): String {
        return gson.toJson(target)
    }

    private fun <T> fromJson(json: String?, type: Class<T>): T? {
        return type.cast(gson.fromJson(json, type))
    }

    private fun <T> fromJson(json: String?, type: Type?): T? {
        return gson.fromJson<T>(json, type)
    }
}