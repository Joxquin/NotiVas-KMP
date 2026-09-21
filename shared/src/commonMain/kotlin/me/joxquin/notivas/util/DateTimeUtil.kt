package me.joxquin.notivas.util

data class DateComponents(
    val year: Int,
    val month: Int,
    val day: Int
) : Comparable<DateComponents> {
    override fun compareTo(other: DateComponents): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    fun toIsoDateString(): String {
        val m = if (month < 10) "0$month" else "$month"
        val d = if (day < 10) "0$day" else "$day"
        return "$year-$m-$d"
    }
}

data class DateTimeComponents(
    val date: DateComponents,
    val hour: Int,
    val minute: Int,
    val epochMillis: Long
)

expect object DateTimeUtil {
    fun nowEpochMillis(): Long
    fun nowLocalDate(): DateComponents
    fun parseIsoToComponents(isoString: String): DateTimeComponents?
    fun addDays(date: DateComponents, days: Int): DateComponents
    fun dayOfWeek(date: DateComponents): Int // 1 = Lunes, 7 = Domingo
    fun dayOfWeekName(date: DateComponents): String // "LUN", "MAR", etc.
    fun formatFullDate(date: DateComponents): String // ej. "Jueves 13 de Marzo"
    fun formatMonthYear(date: DateComponents): String // ej. "Marzo 2026"
}
