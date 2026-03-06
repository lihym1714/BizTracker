package com.biztracker.data.repository

import com.biztracker.data.local.Entry
import com.biztracker.data.local.EntryDao
import com.biztracker.domain.CategorySum
import com.biztracker.domain.DailySum
import com.biztracker.domain.EntryHistory
import com.biztracker.domain.EntryNoteCodec
import com.biztracker.domain.Constants
import com.biztracker.domain.Summary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EntryRepository(
    private val entryDao: EntryDao,
) {
    suspend fun addEntry(
        businessId: Long,
        categoryId: Long?,
        type: String,
        amount: Long,
        occurredDate: String,
        note: String? = null,
    ): Long {
        return entryDao.insert(
            Entry(
                businessId = businessId,
                categoryId = categoryId,
                type = type,
                amount = amount,
                occurredDate = occurredDate,
                note = note,
            )
        )
    }

    suspend fun updateEntry(entry: Entry) {
        entryDao.update(entry)
    }

    suspend fun deleteEntry(entry: Entry) {
        entryDao.delete(entry)
    }

    fun entries(businessId: Long): Flow<List<Entry>> {
        return entryDao.getEntriesByDateDesc(businessId)
    }

    fun todaySummary(businessId: Long, date: String): Flow<Summary> {
        return entries(businessId).map {
            Summary(
                income = entryDao.getIncomeSumByDate(businessId, date) ?: 0L,
                expense = entryDao.getExpenseSumByDate(businessId, date) ?: 0L,
            )
        }
    }

    fun monthSummary(businessId: Long, yearMonth: String): Flow<Summary> {
        return entries(businessId).map {
            Summary(
                income = entryDao.getIncomeSumByMonth(businessId, yearMonth) ?: 0L,
                expense = entryDao.getExpenseSumByMonth(businessId, yearMonth) ?: 0L,
            )
        }
    }

    suspend fun dailySumsForMonth(businessId: Long, yearMonth: String): List<DailySum> {
        return entryDao.getDailySumsForMonth(businessId, yearMonth).map { row ->
            DailySum(
                date = row.date,
                income = row.incomeSum,
                expense = row.expenseSum,
            )
        }
    }

    suspend fun expenseSumsByCategoryForMonth(
        businessId: Long,
        yearMonth: String,
    ): List<CategorySum> {
        return entryDao.getExpenseSumsByCategoryForMonth(businessId, yearMonth).map { row ->
            CategorySum(
                categoryId = row.categoryId,
                categoryName = row.categoryName,
                sum = row.sum,
            )
        }
    }

    suspend fun entryHistoryForDate(
        businessId: Long,
        date: String,
    ): List<EntryHistory> {
        return entryDao.getEntryHistoryByDate(businessId, date).map { row ->
            val (paymentMethod, memo) = EntryNoteCodec.split(
                note = row.note,
                defaultPaymentMethod = Constants.PAYMENT_CASH,
            )
            EntryHistory(
                id = row.id,
                type = row.type,
                amount = row.amount,
                occurredDate = row.occurredDate,
                categoryName = row.categoryName.orEmpty(),
                paymentMethod = paymentMethod,
                memo = memo,
            )
        }
    }
}
