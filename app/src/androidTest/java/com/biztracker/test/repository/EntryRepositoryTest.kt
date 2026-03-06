package com.biztracker.test.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.biztracker.data.local.AppDatabase
import com.biztracker.data.local.Business
import com.biztracker.data.local.Category
import com.biztracker.data.local.EntryType
import com.biztracker.data.repository.EntryRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntryRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: EntryRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = EntryRepository(database.entryDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addEntryWorks() = runBlocking {
        val businessId = insertBusiness()
        val categoryId = insertCategory(businessId, EntryType.INCOME, "Sales")

        val entryId = repository.addEntry(
            businessId = businessId,
            categoryId = categoryId,
            type = EntryType.INCOME,
            amount = 2500L,
            occurredDate = "2026-02-18",
            note = "direct",
        )

        val inserted = database.entryDao().getById(entryId)
        assertEquals(2500L, inserted?.amount)
        assertEquals(EntryType.INCOME, inserted?.type)
    }

    @Test
    fun todaySummaryCorrect() = runBlocking {
        val businessId = insertBusiness()
        val incomeCategoryId = insertCategory(businessId, EntryType.INCOME, "Sales")
        val expenseCategoryId = insertCategory(businessId, EntryType.EXPENSE, "Rent")
        val date = "2026-02-21"

        repository.addEntry(
            businessId = businessId,
            categoryId = incomeCategoryId,
            type = EntryType.INCOME,
            amount = 3000L,
            occurredDate = date,
        )
        repository.addEntry(
            businessId = businessId,
            categoryId = expenseCategoryId,
            type = EntryType.EXPENSE,
            amount = 1200L,
            occurredDate = date,
        )

        val summary = repository.todaySummary(businessId, date).first()
        assertEquals(3000L, summary.income)
        assertEquals(1200L, summary.expense)
    }

    @Test
    fun monthSummaryCorrect() = runBlocking {
        val businessId = insertBusiness()
        val incomeCategoryId = insertCategory(businessId, EntryType.INCOME, "Service")
        val expenseCategoryId = insertCategory(businessId, EntryType.EXPENSE, "Supplies")

        repository.addEntry(
            businessId = businessId,
            categoryId = incomeCategoryId,
            type = EntryType.INCOME,
            amount = 2000L,
            occurredDate = "2026-02-05",
        )
        repository.addEntry(
            businessId = businessId,
            categoryId = expenseCategoryId,
            type = EntryType.EXPENSE,
            amount = 600L,
            occurredDate = "2026-02-17",
        )
        repository.addEntry(
            businessId = businessId,
            categoryId = incomeCategoryId,
            type = EntryType.INCOME,
            amount = 400L,
            occurredDate = "2026-03-01",
        )

        val summary = repository.monthSummary(businessId, "2026-02").first()
        assertEquals(2000L, summary.income)
        assertEquals(600L, summary.expense)
    }

    @Test
    fun flowEmitsUpdates() = runBlocking {
        val businessId = insertBusiness()
        val expenseCategoryId = insertCategory(businessId, EntryType.EXPENSE, "Transport")

        val emissionsDeferred = async {
            repository.entries(businessId).take(2).toList()
        }

        repository.addEntry(
            businessId = businessId,
            categoryId = expenseCategoryId,
            type = EntryType.EXPENSE,
            amount = 150L,
            occurredDate = "2026-02-22",
        )

        val emissions = emissionsDeferred.await()
        assertTrue(emissions[0].isEmpty())
        assertEquals(1, emissions[1].size)
        assertEquals(150L, emissions[1][0].amount)
    }

    private suspend fun insertBusiness(): Long {
        return database.businessDao().insert(
            Business(
                name = "Repo Test Business",
                createdDate = "2026-02-01",
            )
        )
    }

    private suspend fun insertCategory(
        businessId: Long,
        type: String,
        name: String,
    ): Long {
        return database.categoryDao().insert(
            Category(
                businessId = businessId,
                name = name,
                type = type,
                isCustom = true,
            )
        )
    }
}
