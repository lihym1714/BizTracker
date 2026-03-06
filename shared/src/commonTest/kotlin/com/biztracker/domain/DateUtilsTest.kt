package com.biztracker.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class DateUtilsTest {
    @Test
    fun yearMonth_parsesDate() {
        assertEquals("2026-03", DateUtils.yearMonth("2026-03-03"))
    }

    @Test
    fun startAndEndOfMonth_returnExpectedDates() {
        assertEquals("2024-02-01", DateUtils.startOfMonth("2024-02"))
        assertEquals("2024-02-29", DateUtils.endOfMonth("2024-02"))
    }

    @Test
    fun formatCurrency_addsGroupingSeparators() {
        assertEquals("1,234,567", DateUtils.formatCurrency(1_234_567))
        assertEquals("-12,345", DateUtils.formatCurrency(-12_345))
    }
}
