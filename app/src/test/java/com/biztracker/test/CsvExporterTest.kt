package com.biztracker.test

import com.biztracker.domain.Constants
import com.biztracker.export.CsvEntryRow
import com.biztracker.export.CsvExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {
    private val exporter = CsvExporter()

    @Test
    fun exportEntries_includesBomAndEscapesValues() {
        val csv = exporter.exportEntries(
            rows = listOf(
                CsvEntryRow(
                    date = "2026-03-01",
                    type = Constants.TYPE_INCOME,
                    amount = 120000,
                    categoryName = "Sales",
                    paymentMethod = Constants.PAYMENT_TRANSFER,
                    memo = "order \"A-1\", online",
                )
            ),
            includeUtf8Bom = true,
        )

        assertTrue(csv.startsWith("\uFEFF"))
        assertTrue(csv.contains("date,type,amount,categoryName,paymentMethod,memo"))
        assertTrue(csv.contains("\"order \"\"A-1\"\", online\""))
    }

    @Test
    fun exportSettlementEntries_writesSummaryAndDetailRows() {
        val csv = exporter.exportSettlementEntries(
            rows = listOf(
                CsvEntryRow(
                    date = "2026-03-01",
                    type = Constants.TYPE_INCOME,
                    amount = 300000,
                    categoryName = "Sales",
                    paymentMethod = Constants.PAYMENT_CARD,
                    memo = "",
                ),
                CsvEntryRow(
                    date = "2026-03-02",
                    type = Constants.TYPE_EXPENSE,
                    amount = 50000,
                    categoryName = "Supplies",
                    paymentMethod = Constants.PAYMENT_CASH,
                    memo = "paper",
                ),
            ),
            yearMonth = "2026-03",
            includeUtf8Bom = false,
        )

        assertFalse(csv.startsWith("\uFEFF"))

        val lines = csv.lines()
        assertEquals(
            "section,month,date,type,income,expense,profit,categoryName,paymentMethod,memo",
            lines[0],
        )
        assertEquals("SUMMARY,2026-03,,,300000,50000,250000,,,", lines[1])
        assertEquals("DETAIL,2026-03,2026-03-01,INCOME,300000,0,300000,Sales,CARD,", lines[2])
        assertEquals("DETAIL,2026-03,2026-03-02,EXPENSE,0,50000,-50000,Supplies,CASH,paper", lines[3])
    }
}
