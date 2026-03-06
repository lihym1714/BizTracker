package com.biztracker.test.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.biztracker.data.local.AppDatabase
import com.biztracker.data.local.Business
import com.biztracker.data.local.Category
import com.biztracker.data.local.Entry
import com.biztracker.data.local.EntryDao
import com.biztracker.data.local.EntryType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntryDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var entryDao: EntryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        entryDao = database.entryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertEntry() = runBlocking {
        val businessId = insertBusiness()
        val categoryId = insertCategory(businessId, EntryType.EXPENSE, "Rent")
        val entryId = entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = categoryId,
                type = EntryType.EXPENSE,
                amount = 1200L,
                occurredDate = "2026-02-10",
                note = "monthly rent",
            )
        )

        val inserted = entryDao.getById(entryId)
        assertNotNull(inserted)
        assertEquals(1200L, inserted?.amount)
        assertEquals("2026-02-10", inserted?.occurredDate)
    }

    @Test
    fun updateEntry() = runBlocking {
        val businessId = insertBusiness()
        val categoryId = insertCategory(businessId, EntryType.EXPENSE, "Supplies")
        val entryId = entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = categoryId,
                type = EntryType.EXPENSE,
                amount = 200L,
                occurredDate = "2026-02-11",
                note = "before",
            )
        )
        val initial = entryDao.getById(entryId) ?: error("Entry not found")

        entryDao.update(initial.copy(amount = 350L, note = "after"))

        val updated = entryDao.getById(entryId)
        assertEquals(350L, updated?.amount)
        assertEquals("after", updated?.note)
    }

    @Test
    fun deleteEntry() = runBlocking {
        val businessId = insertBusiness()
        val categoryId = insertCategory(businessId, EntryType.EXPENSE, "Transport")
        val entryId = entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = categoryId,
                type = EntryType.EXPENSE,
                amount = 90L,
                occurredDate = "2026-02-12",
            )
        )
        val inserted = entryDao.getById(entryId) ?: error("Entry not found")

        entryDao.delete(inserted)

        assertNull(entryDao.getById(entryId))
    }

    @Test
    fun getEntriesByDateDescOrdering() = runBlocking {
        val businessId = insertBusiness()
        val expenseCategoryId = insertCategory(businessId, EntryType.EXPENSE, "Ops")
        entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = expenseCategoryId,
                type = EntryType.EXPENSE,
                amount = 100L,
                occurredDate = "2026-02-10",
            )
        )
        entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = expenseCategoryId,
                type = EntryType.EXPENSE,
                amount = 200L,
                occurredDate = "2026-02-12",
            )
        )
        entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = expenseCategoryId,
                type = EntryType.EXPENSE,
                amount = 300L,
                occurredDate = "2026-02-11",
            )
        )

        val ordered = entryDao.getEntriesByDateDesc(businessId).first()
        assertEquals(3, ordered.size)
        assertEquals("2026-02-12", ordered[0].occurredDate)
        assertEquals("2026-02-11", ordered[1].occurredDate)
        assertEquals("2026-02-10", ordered[2].occurredDate)
    }

    @Test
    fun getIncomeSumByDateCorrectness() = runBlocking {
        val businessId = insertBusiness()
        val incomeCategoryId = insertCategory(businessId, EntryType.INCOME, "Sales")
        entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = incomeCategoryId,
                type = EntryType.INCOME,
                amount = 1000L,
                occurredDate = "2026-02-14",
            )
        )
        entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = incomeCategoryId,
                type = EntryType.INCOME,
                amount = 500L,
                occurredDate = "2026-02-14",
            )
        )
        entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = incomeCategoryId,
                type = EntryType.INCOME,
                amount = 999L,
                occurredDate = "2026-02-13",
            )
        )

        val sum = entryDao.getIncomeSumByDate(businessId, "2026-02-14")
        assertEquals(1500L, sum)
    }

    @Test
    fun getExpenseSumByMonthCorrectness() = runBlocking {
        val businessId = insertBusiness()
        val expenseCategoryId = insertCategory(businessId, EntryType.EXPENSE, "Bills")
        entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = expenseCategoryId,
                type = EntryType.EXPENSE,
                amount = 700L,
                occurredDate = "2026-02-01",
            )
        )
        entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = expenseCategoryId,
                type = EntryType.EXPENSE,
                amount = 300L,
                occurredDate = "2026-02-20",
            )
        )
        entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = expenseCategoryId,
                type = EntryType.EXPENSE,
                amount = 900L,
                occurredDate = "2026-03-01",
            )
        )

        val sum = entryDao.getExpenseSumByMonth(businessId, "2026-02")
        assertEquals(1000L, sum)
    }

    private suspend fun insertBusiness(): Long {
        return database.businessDao().insert(
            Business(
                name = "Test Business",
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
