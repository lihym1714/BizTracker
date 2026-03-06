package com.biztracker.test.data

import com.biztracker.data.local.Business
import com.biztracker.data.local.Category
import com.biztracker.data.local.Entry
import com.biztracker.data.local.EntryType

object FakeDataGenerator {
    fun createTestBusiness(): Business {
        return Business(
            name = "Test Business",
            createdDate = "2026-01-01",
        )
    }

    fun createTestCategories(): List<Category> {
        return listOf(
            Category(id = 1L, businessId = 1L, name = "Sales", type = EntryType.INCOME),
            Category(id = 2L, businessId = 1L, name = "Service", type = EntryType.INCOME),
            Category(id = 3L, businessId = 1L, name = "Rent", type = EntryType.EXPENSE),
            Category(id = 4L, businessId = 1L, name = "Supplies", type = EntryType.EXPENSE),
            Category(id = 5L, businessId = 1L, name = "Transport", type = EntryType.EXPENSE),
        )
    }

    fun createTestEntries(count: Int): List<Entry> {
        val dates = listOf(
            "2026-01-03",
            "2026-01-04",
            "2026-01-05",
            "2026-01-06",
            "2026-01-07",
            "2026-01-08",
        )
        val categories = createTestCategories()
        val incomeCategoryIds = categories.filter { it.type == EntryType.INCOME }.map { it.id }
        val expenseCategoryIds = categories.filter { it.type == EntryType.EXPENSE }.map { it.id }

        return (0 until count).map { index ->
            val isIncome = index % 2 == 0
            val type = if (isIncome) EntryType.INCOME else EntryType.EXPENSE
            val categoryId = if (isIncome) {
                incomeCategoryIds[index % incomeCategoryIds.size]
            } else {
                expenseCategoryIds[index % expenseCategoryIds.size]
            }

            Entry(
                businessId = 1L,
                categoryId = categoryId,
                type = type,
                amount = (index + 1) * 100L,
                occurredDate = dates[index % dates.size],
                note = "sample-$index",
            )
        }
    }
}
