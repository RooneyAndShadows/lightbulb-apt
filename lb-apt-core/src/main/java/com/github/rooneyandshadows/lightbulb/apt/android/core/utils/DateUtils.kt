package com.github.rooneyandshadows.lightbulb.apt.android.core.utils

import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class DateUtils {
    companion object {
        private const val defaultFormat = "yyyy-MM-dd HH:mm:ssZ"

        @JvmStatic
        fun getOffsetDateFromString(dateString: String?): OffsetDateTime? {
            val formatter = DateTimeFormatter.ofPattern(defaultFormat)
            return OffsetDateTime.parse(dateString, formatter)
        }
        @JvmStatic
        fun getDateFromString(dateString: String?): Date? {
            if (dateString == null) {
                return null
            }
            val locale = Locale.getDefault()
            return SimpleDateFormat(defaultFormat, locale).parse(dateString)
        }
        @JvmStatic
        fun getOffsetDateString(date: OffsetDateTime?): String? {
            if (date == null) {
                return null;
            }
            val locale = Locale.getDefault()
            val formatter = DateTimeFormatter.ofPattern(defaultFormat, locale)
            return date.format(formatter)
        }
        @JvmStatic
        fun getDateString(date: Date?): String? {
            if (date == null) {
                return null;
            }
            val locale = Locale.getDefault()
            return SimpleDateFormat(defaultFormat, locale).format(date)
        }
    }
}