package me.joxquin.notivas.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSISO8601DateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.timeIntervalSince1970

actual object DateTimeUtil {
    actual fun nowEpochMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }

    actual fun nowLocalDate(): DateComponents {
        val calendar = NSCalendar.currentCalendar
        val components = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            fromDate = NSDate()
        )
        return DateComponents(
            year = components.year.toInt(),
            month = components.month.toInt(),
            day = components.day.toInt()
        )
    }

    actual fun parseIsoToComponents(isoString: String): DateTimeComponents? {
        val formatter = NSISO8601DateFormatter()
        val date = formatter.dateFromString(isoString) ?: return null
        val calendar = NSCalendar.currentCalendar
        val components = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            fromDate = date
        )
        return DateTimeComponents(
            date = DateComponents(
                year = components.year.toInt(),
                month = components.month.toInt(),
                day = components.day.toInt()
            ),
            hour = components.hour.toInt(),
            minute = components.minute.toInt(),
            epochMillis = (date.timeIntervalSince1970 * 1000).toLong()
        )
    }

    actual fun addDays(date: DateComponents, days: Int): DateComponents {
        val calendar = NSCalendar.currentCalendar
        val comp = NSDateComponents().apply {
            year = date.year.toLong()
            month = date.month.toLong()
            day = date.day.toLong()
        }
        val nsDate = calendar.dateFromComponents(comp) ?: return date
        val newDate = calendar.dateByAddingUnit(
            NSCalendarUnitDay,
            value = days.toLong(),
            toDate = nsDate,
            options = 0u
        ) ?: return date
        val res = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay,
            fromDate = newDate
        )
        return DateComponents(res.year.toInt(), res.month.toInt(), res.day.toInt())
    }

    actual fun dayOfWeek(date: DateComponents): Int {
        val calendar = NSCalendar.currentCalendar
        val comp = NSDateComponents().apply {
            year = date.year.toLong()
            month = date.month.toLong()
            day = date.day.toLong()
        }
        val nsDate = calendar.dateFromComponents(comp) ?: return 1
        val weekday = calendar.component(platform.Foundation.NSCalendarUnitWeekday, fromDate = nsDate).toInt()
        // NSWeekday: 1 = Sunday, 2 = Monday, ... 7 = Saturday
        return if (weekday == 1) 7 else weekday - 1
    }

    actual fun dayOfWeekName(date: DateComponents): String {
        return when (dayOfWeek(date)) {
            1 -> "LUN"
            2 -> "MAR"
            3 -> "MIÉ"
            4 -> "JUE"
            5 -> "VIE"
            6 -> "SÁB"
            7 -> "DOM"
            else -> "LUN"
        }
    }

    actual fun formatFullDate(date: DateComponents): String {
        val comp = NSDateComponents().apply {
            year = date.year.toLong()
            month = date.month.toLong()
            day = date.day.toLong()
        }
        val calendar = NSCalendar.currentCalendar
        val nsDate = calendar.dateFromComponents(comp) ?: return "${date.day}/${date.month}/${date.year}"
        val formatter = NSDateFormatter().apply {
            dateFormat = "EEEE d 'de' MMMM"
            locale = NSLocale(localeIdentifier = "es_ES")
        }
        return formatter.stringFromDate(nsDate).replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    actual fun formatMonthYear(date: DateComponents): String {
        val comp = NSDateComponents().apply {
            year = date.year.toLong()
            month = date.month.toLong()
            day = date.day.toLong()
        }
        val calendar = NSCalendar.currentCalendar
        val nsDate = calendar.dateFromComponents(comp) ?: return "${date.month}/${date.year}"
        val formatter = NSDateFormatter().apply {
            dateFormat = "MMMM yyyy"
            locale = NSLocale(localeIdentifier = "es_ES")
        }
        return formatter.stringFromDate(nsDate).replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
