package com.github.rooneyandshadows.lightbulb.apt.android.core.utils

import android.annotation.SuppressLint
import org.joda.time.*
import org.joda.time.format.DateTimeFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
@Suppress("unused","MemberVisibilityCanBePrivate","ConstPropertyName")
class DateUtils {
    companion object {
        const val defaultFormat = "yyyy-MM-dd HH:mm:ss"
        const val defaultFormatWithTimeZone = "yyyy-MM-dd HH:mm:ssZ"
        const val defaultFormatWithoutTime = "yyyy-MM-dd"
        val localTimeZone: String

            get() {
                var offset = SimpleDateFormat("X").format(Date())
                offset = "UTC" + offset.substring(0, 3)
                return offset
            }

        fun now(): Date {
            return Date()
        }

        @JvmOverloads
        fun date(year: Int, month: Int, day: Int = 1, hour: Int = 0, minute: Int = 0, second: Int = 0): Date? {
            val dateTime = LocalDateTime()
                .withYear(year)
                .withMonthOfYear(month)
                .withDayOfMonth(day)
                .withHourOfDay(hour)
                .withMinuteOfHour(minute)
                .withSecondOfMinute(second)
                .withMillisOfSecond(0)
            return convertLocalDateTimeToDate(dateTime)
        }

        fun isDateInRange(testDate: Date, start: Date?, end: Date?): Boolean {
            return isDateInRange(testDate, start, end, true)
        }

        fun isDateInRange(testDate: Date, start: Date?, end: Date?, withTime: Boolean): Boolean {
            var newStart = start
            var newEnd = end
            if (!withTime) {
                newStart = setTimeToDate(newStart, 0, 0, 0)
                newEnd = setTimeToDate(newEnd, 23, 59, 59)
            }
            return !testDate.before(newStart) && !testDate.after(newEnd)
        }

        fun isDateBefore(testDate: Date, target: Date?): Boolean {
            return testDate < target
        }

        fun isDateBeforeOrEqual(testDate: Date, target: Date?): Boolean {
            return testDate <= target
        }

        fun isDateAfter(testDate: Date, target: Date?): Boolean {
            return testDate > target
        }

        fun isDateAfterOrEqual(testDate: Date, target: Date?): Boolean {
            return testDate >= target
        }

        fun isDateEqual(testDate: Date?, target: Date?): Boolean {
            return isDateEqual(testDate, target, true)
        }

        fun isDayToday(testDate: Date?): Boolean {
            return isDateEqual(testDate, now(), false)
        }

        fun isDayYesterday(testDate: Date?): Boolean {
            val yesterdayDate = incrementDate(now(), PeriodTypes.DAY, -1, 0)
            return isDateEqual(testDate, yesterdayDate, false)
        }

        fun isDayTomorrow(testDate: Date?): Boolean {
            val tomorrowDate = incrementDate(now(), PeriodTypes.DAY, 1, 0)
            return isDateEqual(testDate, tomorrowDate, false)
        }

        fun isDayAfterTomorrow(testDate: Date?): Boolean {
            val dayAfterTomorowDate = incrementDate(now(), PeriodTypes.DAY, 2, 0)
            return isDateEqual(testDate, dayAfterTomorowDate, false)
        }

        fun isDateEqual(testDate: Date?, target: Date?, withTime: Boolean): Boolean {
            var newTestDate = testDate
            var newTargetDate = target
            if (newTestDate == null || newTargetDate == null){
                return newTestDate === newTargetDate
            }
            if (!withTime) {
                newTestDate = setTimeToDate(newTestDate, 0, 0, 0)
                newTargetDate = setTimeToDate(newTargetDate, 0, 0, 0)
            }
            return newTestDate!!.compareTo(newTargetDate) == 0
        }

        fun getFirstDayOfMonthDate(date: Date?): Date? {
            if (date == null) return null
            val dateTime = LocalDateTime(date).dayOfMonth().withMinimumValue()
            return convertLocalDateTimeToDate(dateTime)
        }

        fun getLastDayOfMonthDate(date: Date?): Date? {
            if (date == null) return null
            val dateTime = LocalDateTime(date).dayOfMonth().withMaximumValue()
            return convertLocalDateTimeToDate(dateTime)
        }

        fun getHourOfDay(date: Date?): Int? {
            if (date == null) return null
            val datetime = LocalDateTime(date)
            return datetime.hourOfDay().get()
        }

        fun getMinuteOfHour(date: Date?): Int? {
            if (date == null) return null
            val datetime = LocalDateTime(date)
            return datetime.minuteOfHour().get()
        }

        fun getSecondOfMinute(date: Date?): Int? {
            if (date == null) return null
            val datetime = LocalDateTime(date)
            return datetime.secondOfMinute().get()
        }

        fun getLongRepresentation(date: Date?): Long? {
            return date?.time
        }

        fun getDateFromLong(dateRepresentation: Long?): Date? {
            return if (dateRepresentation == null) null else Date(dateRepresentation)
        }

        fun getDateWithDayOfWeek(date: Date?, dayOfWeek: Int): Date? {
            val dateTime = LocalDateTime(date)
            return convertLocalDateTimeToDate(dateTime.withDayOfWeek(dayOfWeek))
        }

        fun getDateWithoutTime(date: Date?): Date? {
            if (date == null) return null
            val dateTime = LocalDateTime(date)
            val dateWithoutTime = LocalDate(dateTime)
            return dateWithoutTime.toDate()
        }

        fun getDateFromString(format: String?, dateString: String?): Date? {
            var newFormat = format
            if (dateString == null || dateString == "") return null
            if (newFormat == null || newFormat == "") newFormat = defaultFormat
            val formatter = DateTimeFormat.forPattern(newFormat)
            val dateTime = formatter.parseLocalDateTime(dateString)
            return convertLocalDateTimeToDate(dateTime)
        }

        fun getDateString(format: String?, date: Date?): String? {
            var newFormat = format
            if (date == null) return null
            if (newFormat == null || newFormat == "") newFormat = defaultFormat
            return SimpleDateFormat(newFormat).format(date)
        }

        fun getDateString(format: String?, date: Date?, locale: Locale?): String? {
            var newFormat = format
            if (date == null) return null
            if (newFormat == null || newFormat == "") newFormat = defaultFormat
            return SimpleDateFormat(newFormat, locale).format(date)
        }

        fun getDaysBetweenDates(date1: Date?, date2: Date?): Int {
            val d1 = LocalDate(date1).toDateTimeAtStartOfDay()
            val d2 = LocalDate(date2).toDateTimeAtStartOfDay()
            return Days.daysBetween(d1, d2).days
        }

        fun getAllMonthsForYear(year: Int): ArrayList<Date?> {
            val months = ArrayList<Date?>()
            for (i in 1..12) {
                val newDate = date(year, i, 1)
                months.add(newDate)
            }
            return months
        }

        fun extractDayOfMonthFromDate(date: Date?): Int? {
            if (date == null) return null
            val dateTime = LocalDateTime(date)
            return dateTime.dayOfMonth
        }

        fun extractMonthOfYearFromDate(date: Date?): Int? {
            if (date == null) return null
            val dateTime = LocalDateTime(date)
            return dateTime.monthOfYear
        }

        fun extractYearFromDate(date: Date?): Int? {
            if (date == null) return null
            val dateTime = LocalDateTime(date)
            return dateTime.year
        }

        fun getPeriodsInInterval(startDate: Date?, endDate: Date?, type: PeriodTypes?): Int? {
            if (startDate == null || endDate == null) return null
            var periods = 0
            val start = LocalDateTime(startDate)
            val end = LocalDateTime(endDate)
            when (type) {
                PeriodTypes.DAY -> periods = Days.daysBetween(start, end).days
                PeriodTypes.WEEK -> periods = Weeks.weeksBetween(start, end).weeks
                PeriodTypes.MONTH -> periods = Months.monthsBetween(start, end).months
                PeriodTypes.YEAR -> periods = Years.yearsBetween(start, end).years
                else -> {}
            }
            return periods
        }

        fun setTimeToDate(date: Date?, hour: Int, minutes: Int, seconds: Int): Date? {
            if (date == null) return null
            val dateTime = LocalDateTime(date).withHourOfDay(hour).withMinuteOfHour(minutes).withSecondOfMinute(seconds)
            return convertLocalDateTimeToDate(dateTime)
        }

        fun setYearToDate(date: Date?, year: Int): Date? {
            if (date == null) return null
            val dateTime = LocalDateTime(date).withYear(year)
            return convertLocalDateTimeToDate(dateTime)
        }

        fun setMonthToDate(date: Date?, month: Int): Date? {
            var newMonth = month
            if (date == null) return null
            if (newMonth < 1) newMonth = 1
            if (newMonth > 12) newMonth = 12
            val dateTime = LocalDateTime(date).withMonthOfYear(newMonth)
            return convertLocalDateTimeToDate(dateTime)
        }

        fun incrementDate(date: Date?, type: PeriodTypes?, periods: Int, missPeriods: Int): Date? {
            var newDate = date
            if (newDate == null || type == null) return null
            newDate = when (type) {
                PeriodTypes.DAY -> addDays(newDate, periods, missPeriods)
                PeriodTypes.WEEK -> addWeeks(newDate, periods, missPeriods)
                PeriodTypes.MONTH -> addMonths(newDate, periods, missPeriods)
                PeriodTypes.YEAR -> addYears(newDate, periods, missPeriods)
            }
            return newDate
        }

        fun addSeconds(date: Date?, seconds: Int): Date? {
            if (date == null) return null
            val dateTime = LocalDateTime(date)
                .plusSeconds(seconds)
            return convertLocalDateTimeToDate(dateTime)
        }

        fun addMinutes(date: Date?, minutes: Int): Date? {
            if (date == null) return null
            val dateTime = LocalDateTime(date)
                .plusMinutes(minutes)
            return convertLocalDateTimeToDate(dateTime)
        }

        fun addHours(date: Date?, hours: Int): Date? {
            if (date == null) return null
            val dateTime = LocalDateTime(date)
                .plusHours(hours)
            return convertLocalDateTimeToDate(dateTime)
        }

        private fun addDays(date: Date, days: Int, missDays: Int): Date? {
            var newMissDays = missDays
            var dateTime = LocalDateTime(date)
            if (newMissDays < 0) newMissDays = 0
            newMissDays += 1
            dateTime = dateTime.plusDays(days * newMissDays)
            return convertLocalDateTimeToDate(dateTime)
        }

        private fun addWeeks(date: Date, weeks: Int, missWeeks: Int): Date? {
            var newMissWeeks = missWeeks
            var dateTime = LocalDateTime(date)
            if (newMissWeeks < 0) newMissWeeks = 0
            newMissWeeks += 1
            val daysToAdd = 7 * weeks * newMissWeeks
            dateTime = dateTime.plusDays(daysToAdd)
            return convertLocalDateTimeToDate(dateTime)
        }

        private fun addMonths(date: Date, months: Int, missMonths: Int): Date? {
            var newMissMonths = missMonths
            var dateTime = LocalDateTime(date)
            if (newMissMonths < 0) newMissMonths = 0
            newMissMonths += 1
            dateTime = dateTime.plusMonths(months * newMissMonths)
            return convertLocalDateTimeToDate(dateTime)
        }

        private fun addYears(date: Date, years: Int, missYears: Int): Date? {
            var newMissYears = missYears
            var dateTime = LocalDateTime(date)
            if (newMissYears < 0) newMissYears = 0
            newMissYears += 1
            dateTime = dateTime.plusYears(years * newMissYears)
            return convertLocalDateTimeToDate(dateTime)
        }

        fun getDateObjectByYearAndMonth(year: Int, month: Int): Date {
            return LocalDateTime().withYear(year).withMonthOfYear(month).dayOfMonth().withMinimumValue()
                .withTime(0, 0, 0, 0).toDate()
        }

        private fun convertLocalDateTimeToDate(dateTime: LocalDateTime): Date? {
            return try {
                val formatter = DateTimeFormat.forPattern(defaultFormat)
                val stringToParse = formatter.print(dateTime)
                val outFormat = SimpleDateFormat(defaultFormat)
                outFormat.parse(stringToParse)
            } catch (e: ParseException) {
                null
            }
        }

        enum class PeriodTypes(val value: Int) {
            DAY(1),
            WEEK(2),
            MONTH(3),
            YEAR(4);

            companion object {
                fun valueOf(periodTypeValue: Int): PeriodTypes? {
                   return values().firstOrNull { it.value == periodTypeValue }
                }
            }
        }
    }
}