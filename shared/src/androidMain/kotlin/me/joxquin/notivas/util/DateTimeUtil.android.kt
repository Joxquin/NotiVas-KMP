package me.joxquin.notivas.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

actual object DateTimeUtil {
    actual fun nowEpochMillis(): Long = System.currentTimeMillis()

    actual fun nowLocalDate(): DateComponents {
        val now = LocalDate.now(ZoneId.systemDefault())
        return DateComponents(now.year, now.monthValue, now.dayOfMonth)
    }

    actual fun parseIsoToComponents(isoString: String): DateTimeComponents? {
        return try {
            val zdt = ZonedDateTime.parse(isoString).withZoneSameInstant(ZoneId.systemDefault())
            DateTimeComponents(
                date = DateComponents(zdt.year, zdt.monthValue, zdt.dayOfMonth),
                hour = zdt.hour,
                minute = zdt.minute,
                epochMillis = zdt.toInstant().toEpochMilli()
            )
        } catch (_: Exception) {
            try {
                val instant = Instant.parse(isoString)
                val zdt = instant.atZone(ZoneId.systemDefault())
                DateTimeComponents(
                    date = DateComponents(zdt.year, zdt.monthValue, zdt.dayOfMonth),
                    hour = zdt.hour,
                    minute = zdt.minute,
                    epochMillis = instant.toEpochMilli()
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    actual fun addDays(date: DateComponents, days: Int): DateComponents {
        val ld = LocalDate.of(date.year, date.month, date.day).plusDays(days.toLong())
        return DateComponents(ld.year, ld.monthValue, ld.dayOfMonth)
    }

    actual fun dayOfWeek(date: DateComponents): Int {
        val ld = LocalDate.of(date.year, date.month, date.day)
        return ld.dayOfWeek.value
    }

    actual fun dayOfWeekName(date: DateComponents): String {
        val ld = LocalDate.of(date.year, date.month, date.day)
        return when (ld.dayOfWeek) {
            DayOfWeek.MONDAY -> "LUN"
            DayOfWeek.TUESDAY -> "MAR"
            DayOfWeek.WEDNESDAY -> "MIÉ"
            DayOfWeek.THURSDAY -> "JUE"
            DayOfWeek.FRIDAY -> "VIE"
            DayOfWeek.SATURDAY -> "SÁB"
            DayOfWeek.SUNDAY -> "DOM"
            else -> "LUN"
        }
    }

    actual fun formatFullDate(date: DateComponents): String {
        val ld = LocalDate.of(date.year, date.month, date.day)
        val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES"))
        return ld.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
    }

    actual fun formatMonthYear(date: DateComponents): String {
        val ld = LocalDate.of(date.year, date.month, date.day)
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
        return ld.format(formatter).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
    }
}
