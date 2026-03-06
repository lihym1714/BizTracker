package com.biztracker.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object DateUtils {
    private val seoulTimeZone = TimeZone.of("Asia/Seoul")

    fun today(): String {
        return Clock.System.now().toLocalDateTime(seoulTimeZone).date.toString()
    }

    fun yearMonth(date: String): String {
        val parts = date.split("-")
        require(parts.size == 3) { "Invalid date format: $date" }
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        require(month in 1..12) { "Invalid month: $month" }
        return formatYearMonth(year, month)
    }

    fun startOfMonth(yearMonth: String): String {
        val (year, month) = parseYearMonth(yearMonth)
        return formatDate(year, month, 1)
    }

    fun endOfMonth(yearMonth: String): String {
        val (year, month) = parseYearMonth(yearMonth)
        return formatDate(year, month, daysInMonth(year, month))
    }

    fun formatCurrency(amount: Long): String {
        val raw = amount.toString()
        val isNegative = raw.startsWith("-")
        val digits = raw.removePrefix("-")
        val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
        return if (isNegative) "-$grouped" else grouped
    }

    private fun parseYearMonth(yearMonth: String): Pair<Int, Int> {
        val parts = yearMonth.split("-")
        require(parts.size == 2) { "Invalid year-month format: $yearMonth" }
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        require(month in 1..12) { "Invalid month: $month" }
        return year to month
    }

    private fun formatYearMonth(year: Int, month: Int): String {
        return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}"
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        return buildString {
            append(year.toString().padStart(4, '0'))
            append('-')
            append(month.toString().padStart(2, '0'))
            append('-')
            append(day.toString().padStart(2, '0'))
        }
    }

    private fun daysInMonth(year: Int, month: Int): Int {
        return when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> error("Invalid month: $month")
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}
