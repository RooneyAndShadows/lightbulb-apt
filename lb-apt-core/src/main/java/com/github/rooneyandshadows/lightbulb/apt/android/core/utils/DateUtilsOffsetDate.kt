package com.github.rooneyandshadows.lightbulb.apt.android.core.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.*

@SuppressLint("SimpleDateFormat")
@Suppress("unused", "MemberVisibilityCanBePrivate", "ConstPropertyName")
class DateUtilsOffsetDate {
    companion object {
        const val defaultFormat = "yyyy-MM-dd HH:mm:ss"
        const val defaultFormatWithTimeZone = "yyyy-MM-dd HH:mm:ssZ"
        const val defaultFormatWithoutTime = "yyyy-MM-dd"
        val localTimeZone: String
            get() {
                var offset = SimpleDateFormat("X").format(Date())
                offset = offset.substring(0, 3)
                return offset
            }

        fun nowLocal(): OffsetDateTime {
            return now(ZoneOffset.systemDefault())
        }

        fun nowUTC(): OffsetDateTime {
            return now(ZoneOffset.UTC)
        }

        fun now(timezone: ZoneOffset?): OffsetDateTime {
            return OffsetDateTime.now(timezone)
        }

        fun now(timezone: ZoneId?): OffsetDateTime {
            return OffsetDateTime.now(timezone)
        }

        fun toUTC(date: OffsetDateTime?): OffsetDateTime? {
            return toTimeZone(date, ZoneOffset.UTC)
        }

        fun toTimeZone(date: OffsetDateTime?, timeZone: ZoneOffset?): OffsetDateTime? {
            return date?.withOffsetSameInstant(timeZone)
        }

        fun toDate(date: OffsetDateTime): Date {
            return Date(date.toInstant().toEpochMilli())
        }

        @JvmOverloads
        fun fromDate(date: Date, toTimezone: String? = localTimeZone): OffsetDateTime {
            return OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(date.toInstant().toEpochMilli()),
                ZoneOffset.of(toTimezone)
            )
        }

        @JvmOverloads
        fun date(
            year: Int,
            month: Int,
            day: Int = 1,
            hour: Int = 0,
            minute: Int = 0,
            second: Int = 0,
            timezone: ZoneOffset? = ZoneOffset.of(
                localTimeZone
            )
        ): OffsetDateTime {
            return OffsetDateTime.of(year, month, day, hour, minute, second, 0, timezone)
        }

        fun isDateInRange(testDate: OffsetDateTime, start: OffsetDateTime?, end: OffsetDateTime?): Boolean {
            return isDateInRange(testDate, start, end, true)
        }

        fun isDateInRange(
            testDate: OffsetDateTime,
            start: OffsetDateTime?,
            end: OffsetDateTime?,
            withTime: Boolean
        ): Boolean {
            var newStart = start
            var newEnd = end
            if (!withTime) {
                newStart = setTimeToDate(newStart, 0, 0, 0)
                newEnd = setTimeToDate(newEnd, 23, 59, 59)
            }
            return !testDate.isBefore(newStart) && !testDate.isAfter(newEnd)
        }

        fun isDateBefore(testDate: OffsetDateTime, target: OffsetDateTime?): Boolean {
            return testDate < target
        }

        fun isDateBeforeOrEqual(testDate: OffsetDateTime, target: OffsetDateTime?): Boolean {
            return testDate <= target
        }

        fun isDateAfter(testDate: OffsetDateTime, target: OffsetDateTime?): Boolean {
            return testDate > target
        }

        fun isDateAfterOrEqual(testDate: OffsetDateTime, target: OffsetDateTime?): Boolean {
            return testDate >= target
        }

        fun isDateEqual(testDate: OffsetDateTime?, target: OffsetDateTime?): Boolean {
            return isDateEqual(testDate, target, true)
        }

        fun isDayToday(testDate: OffsetDateTime): Boolean {
            return isDateEqual(testDate, now(testDate.offset), false)
        }

        fun isDayYesterday(testDate: OffsetDateTime): Boolean {
            val yesterdayDate = incrementDate(now(testDate.offset), PeriodTypes.DAY, -1, 0)
            return isDateEqual(testDate, yesterdayDate, false)
        }

        fun isDayTomorrow(testDate: OffsetDateTime): Boolean {
            val tomorrowDate = incrementDate(now(testDate.offset), PeriodTypes.DAY, 1, 0)
            return isDateEqual(testDate, tomorrowDate, false)
        }

        fun isDayAfterTomorrow(testDate: OffsetDateTime): Boolean {
            val dayAfterTomorrowDate = incrementDate(now(testDate.offset), PeriodTypes.DAY, 2, 0)
            return isDateEqual(testDate, dayAfterTomorrowDate, false)
        }

        fun isDateEqual(testDate: OffsetDateTime?, target: OffsetDateTime?, withTime: Boolean): Boolean {
            var newTestDate = testDate
            var newTarget = target
            if (newTestDate == null || newTarget == null) return newTestDate === newTarget
            if (!withTime) {
                newTestDate = setTimeToDate(newTestDate, 0, 0, 0)
                newTarget = setTimeToDate(newTarget, 0, 0, 0)
            }
            return newTestDate!!.compareTo(newTarget) == 0
        }

        fun getFirstDayOfMonthDate(date: OffsetDateTime?): OffsetDateTime? {
            return date?.with(TemporalAdjusters.firstDayOfMonth())
        }

        fun getLastDayOfMonthDate(date: OffsetDateTime?): OffsetDateTime? {
            return date?.with(TemporalAdjusters.lastDayOfMonth())
        }

        fun getHourOfDay(date: OffsetDateTime?): Int? {
            return date?.hour
        }

        fun getMinuteOfHour(date: OffsetDateTime?): Int? {
            return date?.minute
        }

        fun getSecondOfMinute(date: OffsetDateTime?): Int? {
            return date?.second
        }

        fun getLongRepresentation(date: OffsetDateTime?): Long? {
            return date?.toEpochSecond()
        }

        fun getDateFromLong(dateRepresentation: Long?): OffsetDateTime? {
            return getDateFromLong(
                dateRepresentation, ZoneOffset.of(
                    localTimeZone
                )
            )
        }

        fun getDateFromLong(dateRepresentation: Long?, zoneId: ZoneId?): OffsetDateTime? {
            return if (dateRepresentation == null) null else OffsetDateTime.ofInstant(
                Instant.ofEpochSecond(
                    dateRepresentation
                ), zoneId
            )
        }

        fun getDateWithDayOfWeek(date: OffsetDateTime, dayOfWeek: Int): OffsetDateTime {
            return date.with(DayOfWeek.of(dayOfWeek))
        }

        fun getDateWithoutTime(date: OffsetDateTime?): OffsetDateTime? {
            return date?.withHour(0)?.withMinute(0)?.withSecond(0)?.withNano(0)
        }

        fun getDateFromString(format: String?, dateString: String?): OffsetDateTime? {
            var newFormat = format
            if (dateString == null || dateString == "") return null
            if (newFormat == null || newFormat == "") newFormat = defaultFormat
            val formatter = DateTimeFormatter.ofPattern(newFormat)
            return OffsetDateTime.parse(dateString, formatter)
        }

        fun getDateString(format: String?, date: OffsetDateTime?): String? {
            return getDateString(format, date, Locale.getDefault())
        }

        fun getDateString(format: String?, date: OffsetDateTime?, locale: Locale?): String? {
            var newFormat = format
            if (date == null) return null
            if (newFormat == null || newFormat == "") newFormat = defaultFormat
            val formatter = DateTimeFormatter.ofPattern(newFormat, locale)
            return date.format(formatter)
        }

        fun getDaysBetweenDates(date1: OffsetDateTime?, date2: OffsetDateTime?): Int {
            return getPeriodsInInterval(getDateWithoutTime(date1), getDateWithoutTime(date2), PeriodTypes.DAY)!!
        }

        fun getAllMonthsForYear(year: Int): List<OffsetDateTime> {
            val months = ArrayList<OffsetDateTime>()
            for (i in 1..12) {
                val newDate = date(year, i, 1)
                months.add(newDate)
            }
            return months
        }

        fun extractDayOfMonthFromDate(date: OffsetDateTime?): Int? {
            return date?.dayOfMonth
        }

        fun extractMonthOfYearFromDate(date: OffsetDateTime?): Int {
            return date?.monthValue ?: 0
        }

        fun extractYearFromDate(date: OffsetDateTime?): Int {
            return date?.year ?: 0
        }

        fun getPeriodsInInterval(startDate: OffsetDateTime?, endDate: OffsetDateTime?, type: PeriodTypes?): Int? {
            if (startDate == null || endDate == null) return null
            var periods = 0
            when (type) {
                PeriodTypes.DAY -> periods = ChronoUnit.DAYS.between(startDate, endDate).toInt()
                PeriodTypes.WEEK -> periods = ChronoUnit.WEEKS.between(startDate, endDate).toInt()
                PeriodTypes.MONTH -> periods = ChronoUnit.MONTHS.between(startDate, endDate).toInt()
                PeriodTypes.YEAR -> periods = ChronoUnit.YEARS.between(startDate, endDate).toInt()
                else -> {}
            }
            return periods
        }

        fun setTimeToDate(date: OffsetDateTime?, hour: Int, minutes: Int, seconds: Int): OffsetDateTime? {
            return date?.withHour(hour)?.withMinute(minutes)?.withSecond(seconds)?.withNano(0)
        }

        fun setYearToDate(date: OffsetDateTime?, year: Int): OffsetDateTime? {
            return date?.withYear(year)
        }

        fun setMonthToDate(date: OffsetDateTime?, month: Int): OffsetDateTime? {
            var newMonth = month
            if (date == null) return null
            if (newMonth < 1) newMonth = 1
            if (newMonth > 12) newMonth = 12
            return date.withMonth(newMonth)
        }

        fun incrementDate(date: OffsetDateTime?, type: PeriodTypes?, periods: Int, missPeriods: Int): OffsetDateTime? {
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

        fun addSeconds(date: OffsetDateTime?, seconds: Int): OffsetDateTime? {
            return date?.plusSeconds(seconds.toLong())
        }

        fun addMinutes(date: OffsetDateTime?, minutes: Int): OffsetDateTime? {
            return date?.plusMinutes(minutes.toLong())
        }

        fun addHours(date: OffsetDateTime?, hours: Int): OffsetDateTime? {
            return date?.plusHours(hours.toLong())
        }

        private fun addDays(date: OffsetDateTime, days: Int, missDays: Int): OffsetDateTime {
            var newMissDays = missDays
            if (newMissDays < 0) newMissDays = 0
            newMissDays += 1
            return date.plusDays(days.toLong() * newMissDays)
        }

        private fun addWeeks(date: OffsetDateTime, weeks: Int, missWeeks: Int): OffsetDateTime {
            var newMssWeeks = missWeeks
            if (newMssWeeks < 0) newMssWeeks = 0
            newMssWeeks += 1
            val daysToAdd = 7 * weeks * newMssWeeks
            return date.plusDays(daysToAdd.toLong())
        }

        private fun addMonths(date: OffsetDateTime, months: Int, missMonths: Int): OffsetDateTime {
            var newMissMonths = missMonths
            if (newMissMonths < 0) newMissMonths = 0
            newMissMonths += 1
            return date.plusMonths(months.toLong() * newMissMonths)
        }

        private fun addYears(date: OffsetDateTime, years: Int, missYears: Int): OffsetDateTime {
            var newMissYears = missYears
            if (newMissYears < 0) newMissYears = 0
            newMissYears += 1
            return date.plusYears(years.toLong() * newMissYears)
        }

        fun getDateObjectByYearAndMonth(year: Int, month: Int): OffsetDateTime {
            return date(year, month)
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