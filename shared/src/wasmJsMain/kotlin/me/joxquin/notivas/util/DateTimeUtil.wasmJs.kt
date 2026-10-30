package me.joxquin.notivas.util

import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => Date.now()")
private external fun jsDateNow(): Double

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => new Date().getFullYear()")
private external fun jsGetYear(): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => new Date().getMonth() + 1")
private external fun jsGetMonth(): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => new Date().getDate()")
private external fun jsGetDate(): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(iso) => Date.parse(iso)")
private external fun jsParseDate(iso: String): Double

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(epoch) => new Date(epoch).getFullYear()")
private external fun jsYearFromEpoch(epoch: Double): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(epoch) => new Date(epoch).getMonth() + 1")
private external fun jsMonthFromEpoch(epoch: Double): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(epoch) => new Date(epoch).getDate()")
private external fun jsDateFromEpoch(epoch: Double): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(epoch) => new Date(epoch).getHours()")
private external fun jsHoursFromEpoch(epoch: Double): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(epoch) => new Date(epoch).getMinutes()")
private external fun jsMinutesFromEpoch(epoch: Double): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(epoch) => new Date(epoch).getDay()")
private external fun jsDayOfWeekFromEpoch(epoch: Double): Int

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("(year, month, day, hour, minute) => new Date(year, month - 1, day, hour, minute).getTime()")
private external fun jsToEpochMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Double

actual object DateTimeUtil {
    actual fun nowEpochMillis(): Long = jsDateNow().toLong()

    actual fun nowLocalDate(): DateComponents {
        return DateComponents(jsGetYear(), jsGetMonth(), jsGetDate())
    }

    actual fun parseIsoToComponents(isoString: String): DateTimeComponents? {
        val epoch = jsParseDate(isoString)
        if (epoch.isNaN()) return null
        return DateTimeComponents(
            date = DateComponents(
                year = jsYearFromEpoch(epoch),
                month = jsMonthFromEpoch(epoch),
                day = jsDateFromEpoch(epoch)
            ),
            hour = jsHoursFromEpoch(epoch),
            minute = jsMinutesFromEpoch(epoch),
            epochMillis = epoch.toLong()
        )
    }

    actual fun addDays(date: DateComponents, days: Int): DateComponents {
        val epoch = jsToEpochMillis(date.year, date.month, date.day, 12, 0)
        val newEpoch = epoch + (days.toDouble() * 24 * 60 * 60 * 1000)
        return DateComponents(
            year = jsYearFromEpoch(newEpoch),
            month = jsMonthFromEpoch(newEpoch),
            day = jsDateFromEpoch(newEpoch)
        )
    }

    actual fun dayOfWeek(date: DateComponents): Int {
        val epoch = jsToEpochMillis(date.year, date.month, date.day, 12, 0)
        val day = jsDayOfWeekFromEpoch(epoch) // 0 is Sunday
        return if (day == 0) 7 else day
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
        val months = listOf(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
        )
        val days = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
        val dayName = days.getOrElse(dayOfWeek(date) - 1) { "Día" }
        val monthName = months.getOrElse(date.month - 1) { "mes" }
        return "$dayName ${date.day} de $monthName"
    }

    actual fun formatMonthYear(date: DateComponents): String {
        val months = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        val monthName = months.getOrElse(date.month - 1) { "Mes" }
        return "$monthName ${date.year}"
    }

    actual fun toEpochMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        return jsToEpochMillis(year, month, day, hour, minute).toLong()
    }
}
