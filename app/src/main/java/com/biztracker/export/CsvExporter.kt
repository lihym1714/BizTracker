package com.biztracker.export

import com.biztracker.domain.Constants
import javax.inject.Inject
import javax.inject.Singleton

data class CsvEntryRow(
    val date: String,
    val type: String,
    val amount: Long,
    val categoryName: String,
    val paymentMethod: String,
    val memo: String,
)

@Singleton
class CsvExporter @Inject constructor() {
    fun exportEntries(
        rows: List<CsvEntryRow>,
        includeUtf8Bom: Boolean,
    ): String {
        val builder = StringBuilder()
        if (includeUtf8Bom) {
            builder.append(UTF8_BOM)
        }

        builder.append(HEADER)
        rows.forEach { row ->
            builder
                .append('\n')
                .append(escape(row.date)).append(',')
                .append(escape(row.type)).append(',')
                .append(row.amount).append(',')
                .append(escape(row.categoryName)).append(',')
                .append(escape(row.paymentMethod)).append(',')
                .append(escape(row.memo))
        }
        return builder.toString()
    }

    fun exportSettlementEntries(
        rows: List<CsvEntryRow>,
        yearMonth: String,
        includeUtf8Bom: Boolean,
    ): String {
        val builder = StringBuilder()
        if (includeUtf8Bom) {
            builder.append(UTF8_BOM)
        }

        builder.append(SETTLEMENT_HEADER)

        val totalIncome = rows
            .asSequence()
            .filter { row -> row.type == Constants.TYPE_INCOME }
            .sumOf { row -> row.amount }
        val totalExpense = rows
            .asSequence()
            .filter { row -> row.type == Constants.TYPE_EXPENSE }
            .sumOf { row -> row.amount }
        val totalProfit = totalIncome - totalExpense

        appendSettlementLine(
            builder = builder,
            section = "SUMMARY",
            month = yearMonth,
            date = "",
            type = "",
            income = totalIncome,
            expense = totalExpense,
            profit = totalProfit,
            categoryName = "",
            paymentMethod = "",
            memo = "",
        )

        rows.forEach { row ->
            val income = if (row.type == Constants.TYPE_INCOME) row.amount else 0L
            val expense = if (row.type == Constants.TYPE_EXPENSE) row.amount else 0L
            appendSettlementLine(
                builder = builder,
                section = "DETAIL",
                month = yearMonth,
                date = row.date,
                type = row.type,
                income = income,
                expense = expense,
                profit = income - expense,
                categoryName = row.categoryName,
                paymentMethod = row.paymentMethod,
                memo = row.memo,
            )
        }

        return builder.toString()
    }

    private fun appendSettlementLine(
        builder: StringBuilder,
        section: String,
        month: String,
        date: String,
        type: String,
        income: Long,
        expense: Long,
        profit: Long,
        categoryName: String,
        paymentMethod: String,
        memo: String,
    ) {
        builder
            .append('\n')
            .append(escape(section)).append(',')
            .append(escape(month)).append(',')
            .append(escape(date)).append(',')
            .append(escape(type)).append(',')
            .append(income).append(',')
            .append(expense).append(',')
            .append(profit).append(',')
            .append(escape(categoryName)).append(',')
            .append(escape(paymentMethod)).append(',')
            .append(escape(memo))
    }

    private fun escape(value: String): String {
        val requiresQuote = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        if (!requiresQuote) {
            return value
        }
        return buildString {
            append('"')
            value.forEach { char ->
                if (char == '"') {
                    append("\"\"")
                } else {
                    append(char)
                }
            }
            append('"')
        }
    }

    private companion object {
        const val UTF8_BOM = "\uFEFF"
        const val HEADER = "date,type,amount,categoryName,paymentMethod,memo"
        const val SETTLEMENT_HEADER =
            "section,month,date,type,income,expense,profit,categoryName,paymentMethod,memo"
    }
}
