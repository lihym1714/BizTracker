package com.biztracker.test.debug

import android.content.Context
import androidx.room.Room
import com.biztracker.data.local.AppDatabase
import com.biztracker.data.local.Business
import com.biztracker.data.local.Category
import com.biztracker.data.local.Entry
import com.biztracker.data.local.EntryType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

object SeedDatabaseWorker {
    suspend fun runSeed(context: Context) {
        withContext(Dispatchers.IO) {
            val database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME,
            ).build()

            try {
                database.clearAllTables()

                val businessId = database.businessDao().insert(
                    Business(
                        name = "Seeded Business",
                        createdDate = "2026-01-01",
                    )
                )

                val categories = listOf(
                    Category(businessId = businessId, name = "Sales", type = EntryType.INCOME),
                    Category(businessId = businessId, name = "Service", type = EntryType.INCOME),
                    Category(businessId = businessId, name = "Rent", type = EntryType.EXPENSE),
                    Category(businessId = businessId, name = "Supplies", type = EntryType.EXPENSE),
                    Category(businessId = businessId, name = "Transport", type = EntryType.EXPENSE),
                )

                val categoryIds = categories.map { category ->
                    database.categoryDao().insert(category)
                }

                val incomeCategoryIds = categoryIds.take(2)
                val expenseCategoryIds = categoryIds.drop(2)
                val baseDate = LocalDate.of(2026, 1, 1)

                repeat(50) { index ->
                    val isIncome = index % 2 == 0
                    val type = if (isIncome) EntryType.INCOME else EntryType.EXPENSE
                    val categoryId = if (isIncome) {
                        incomeCategoryIds[index % incomeCategoryIds.size]
                    } else {
                        expenseCategoryIds[index % expenseCategoryIds.size]
                    }

                    database.entryDao().insert(
                        Entry(
                            businessId = businessId,
                            categoryId = categoryId,
                            type = type,
                            amount = 100L + (index * 10L),
                            occurredDate = baseDate.plusDays((index % 20).toLong()).toString(),
                            note = "seed-$index",
                        )
                    )
                }
            } finally {
                database.close()
            }
        }
    }

    suspend fun clearDatabase(context: Context) {
        withContext(Dispatchers.IO) {
            val database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME,
            ).build()
            try {
                database.clearAllTables()
            } finally {
                database.close()
            }
        }
    }

    private const val DATABASE_NAME = "biztracker.db"
}
