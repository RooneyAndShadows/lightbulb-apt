package com.github.rooneyandshadows.lightbulb.apt.android.core.utils

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.SparseArray
import com.github.rooneyandshadows.java.commons.date.DateUtils
import com.github.rooneyandshadows.java.commons.date.DateUtilsOffsetDate
import java.time.OffsetDateTime
import java.util.*
import kotlin.collections.ArrayList

@Suppress("unused")
class ParcelUtils {
    companion object {
        @JvmStatic
        fun writeOffsetDateTime(dest: Parcel, date: OffsetDateTime?): Companion {
            dest.writeByte((if (date == null) 0 else 1).toByte())
            if (date != null) dest.writeString(
                DateUtilsOffsetDate.getDateString(
                    DateUtilsOffsetDate.defaultFormatWithTimeZone,
                    date
                )
            )
            return Companion
        }

        @JvmStatic
        fun writeDate(dest: Parcel, date: Date?): Companion {
            dest.writeByte((if (date == null) 0 else 1).toByte())
            if (date != null) dest.writeString(
                DateUtils.getDateString(
                    DateUtils.defaultFormatWithTimeZone,
                    date
                )
            )
            return Companion
        }

        @JvmStatic
        fun writeInt(dest: Parcel, integer: Int?): Companion {
            dest.writeByte((if (integer == null) 0 else 1).toByte())
            if (integer != null) dest.writeInt(integer)
            return Companion
        }

        @JvmStatic
        fun writeString(dest: Parcel, string: String?): Companion {
            dest.writeByte((if (string == null) 0 else 1).toByte())
            if (string != null) dest.writeString(string)
            return Companion
        }

        @JvmStatic
        fun writeDouble(dest: Parcel, value: Double?): Companion {
            dest.writeByte((if (value == null) 0 else 1).toByte())
            if (value != null) dest.writeDouble(value)
            return Companion
        }

        @JvmStatic
        fun writeFloat(dest: Parcel, value: Float?): Companion {
            dest.writeByte((if (value == null) 0 else 1).toByte())
            if (value != null) dest.writeFloat(value)
            return Companion
        }

        @JvmStatic
        fun writeBoolean(dest: Parcel, bool: Boolean?): Companion {
            dest.writeByte((if (bool == null) 0 else 1).toByte())
            if (bool != null) dest.writeInt(if (bool) 1 else 0)
            return Companion
        }

        @JvmStatic
        fun writeUUID(dest: Parcel, uuid: UUID?): Companion {
            dest.writeByte((if (uuid == null) 0 else 1).toByte())
            if (uuid != null) dest.writeString(uuid.toString())
            return Companion
        }

        @JvmStatic
        fun writeParcelable(dest: Parcel, parcelable: Parcelable?): Companion {
            dest.writeByte((if (parcelable == null) 0 else 1).toByte())
            if (parcelable != null) dest.writeParcelable(parcelable, 0)
            return Companion
        }

        @JvmStatic
        fun writeStringArrayList(dest: Parcel, stringList: ArrayList<String>?): Companion {
            dest.writeStringList(stringList)
            return Companion
        }

        @JvmStatic
        fun <T : Any> writeSparseArray(
            dest: Parcel,
            list: SparseArray<T>?,
        ): Companion {
            dest.writeByte((if (list == null) 0 else 1).toByte())
            if (list != null) dest.writeSparseArray(list)
            return Companion
        }

        @JvmStatic
        fun <T : Any> writeArrayList(
            dest: Parcel,
            list: ArrayList<T>?,
        ): Companion {
            dest.writeByte((if (list == null) 0 else 1).toByte())
            if (list != null) dest.writeList(list)
            return Companion
        }

        @JvmStatic
        fun <T : Parcelable> writeTypedArrayList(
            dest: Parcel,
            list: ArrayList<T>?,
        ): Companion {
            dest.writeByte((if (list == null) 0 else 1).toByte())
            if (list != null) dest.writeTypedList(list)
            return Companion
        }

        @JvmStatic
        fun <K : Any, V : Any> writeHashMap(dest: Parcel, map: HashMap<K, V>?): Companion {
            dest.writeMap(map)
            return Companion
        }

        @JvmStatic
        fun readOffsetDateTime(source: Parcel): OffsetDateTime? {
            return if (source.readByte().toInt() == 1) DateUtilsOffsetDate.getDateFromString(
                DateUtilsOffsetDate.defaultFormatWithTimeZone, source.readString()
            ) else null
        }

        @JvmStatic
        fun readDate(source: Parcel): Date? {
            return if (source.readByte().toInt() == 1) DateUtils.getDateFromString(
                DateUtils.defaultFormatWithTimeZone,
                source.readString()
            ) else null
        }

        @JvmStatic
        fun readInt(source: Parcel): Int? {
            return if (source.readByte().toInt() == 1) source.readInt() else null
        }

        @JvmStatic
        fun readString(source: Parcel): String? {
            return if (source.readByte().toInt() == 1) source.readString() else null
        }

        @JvmStatic
        fun readDouble(source: Parcel): Double? {
            return if (source.readByte().toInt() == 1) source.readDouble() else null
        }

        @JvmStatic
        fun readFloat(source: Parcel): Float? {
            return if (source.readByte().toInt() == 1) source.readFloat() else null
        }

        @JvmStatic
        fun readBoolean(source: Parcel): Boolean? {
            return if (source.readByte().toInt() == 1) source.readInt() == 1 else null
        }

        @JvmStatic
        fun readUUID(source: Parcel): UUID? {
            return if (source.readByte().toInt() == 1)
                UUID.fromString(source.readString()) else null
        }

        @Suppress("DEPRECATION")
        @JvmStatic
        fun <T : Parcelable> readParcelable(source: Parcel, clazz: Class<T>): T? {
            return if (source.readByte().toInt() == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    source.readParcelable(ParcelUtils::class.java.classLoader, clazz)
                else
                    source.readParcelable(ParcelUtils::class.java.classLoader)
            } else null
        }

        @JvmStatic
        fun readStringList(source: Parcel): ArrayList<String>? {
            return source.createStringArrayList()
        }

        @Suppress("DEPRECATION")
        @JvmStatic
        fun <V : Any> readSparseArray(source: Parcel, clazz: Class<V>): SparseArray<V> {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                source.readSparseArray(ParcelUtils::class.java.classLoader, clazz) ?: SparseArray()
            else
                source.readSparseArray(ParcelUtils::class.java.classLoader) ?: SparseArray()
        }

        @Suppress("DEPRECATION")
        @JvmStatic
        fun <V : Any> readArrayList(
            source: Parcel,
            clazz: Class<V>,
        ): ArrayList<V> {
            return ArrayList<V>().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    source.readList(this, ParcelUtils::class.java.classLoader, clazz)
                else
                    source.readList(this, ParcelUtils::class.java.classLoader)
            }
        }

        @Suppress("DEPRECATION")
        @JvmStatic
        fun <V : Parcelable> readTypedArrayList(
            source: Parcel,
            creator: Parcelable.Creator<V>,
        ): ArrayList<V> {
            return ArrayList<V>().apply {
                source.readTypedList(this, creator)
            }
        }

        @Suppress("DEPRECATION")
        @JvmStatic
        fun <K : Any, V : Any> readHashMap(
            source: Parcel,
            keyClass: Class<K>,
            valKey: Class<V>,
        ): HashMap<K, V> {
            return HashMap<K, V>().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    source.readMap(this, ParcelUtils::class.java.classLoader, keyClass, valKey)
                else
                    source.readMap(this, ParcelUtils::class.java.classLoader)
            }
        }
    }
}