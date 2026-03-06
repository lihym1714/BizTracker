package com.biztracker.test

import com.biztracker.domain.DateUtils
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {
    @Test
    fun todayFormatCorrect() {
        val today = DateUtils.today()
        assertTrue(today.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        LocalDate.parse(today)
    }

    @Test
    fun yearMonthExtractionCorrect() {
        val yearMonth = DateUtils.yearMonth("2026-02-27")
        assertEquals("2026-02", yearMonth)
    }

    @Test
    fun startOfMonthEndOfMonthCorrect() {
        val start = DateUtils.startOfMonth("2024-02")
        val end = DateUtils.endOfMonth("2024-02")

        assertEquals("2024-02-01", start)
        assertEquals("2024-02-29", end)
    }
}
