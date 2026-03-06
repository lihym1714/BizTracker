package com.biztracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert
    suspend fun insert(entry: Entry): Long

    @Update
    suspend fun update(entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    fun getById(id: Long): Entry?

    @Query(
        """
        SELECT *
        FROM entries
        WHERE businessId = :businessId
        ORDER BY occurredDate DESC, id DESC
        """
    )
    fun getEntriesByDateDesc(businessId: Long): Flow<List<Entry>>

    @Query(
        """
        SELECT *
        FROM entries
        WHERE businessId = :businessId
          AND occurredDate BETWEEN :start AND :end
        ORDER BY occurredDate DESC, id DESC
        """
    )
    fun getEntriesBetweenDates(businessId: Long, start: String, end: String): List<Entry>

    @Query(
        """
        SELECT
            e.id AS id,
            e.type AS type,
            e.amount AS amount,
            e.occurredDate AS occurredDate,
            e.note AS note,
            c.name AS categoryName
        FROM entries e
        LEFT JOIN categories c ON c.id = e.categoryId
        WHERE e.businessId = :businessId
          AND e.occurredDate = :date
        ORDER BY e.id DESC
        """
    )
    suspend fun getEntryHistoryByDate(businessId: Long, date: String): List<EntryHistoryRow>

    @Query(
        """
        SELECT SUM(amount)
        FROM entries
        WHERE businessId = :businessId
          AND occurredDate = :date
          AND type = '${EntryType.INCOME}'
        """
    )
    suspend fun getIncomeSumByDate(businessId: Long, date: String): Long?

    @Query(
        """
        SELECT SUM(amount)
        FROM entries
        WHERE businessId = :businessId
          AND occurredDate = :date
          AND type = '${EntryType.EXPENSE}'
        """
    )
    suspend fun getExpenseSumByDate(businessId: Long, date: String): Long?

    @Query(
        """
        SELECT SUM(amount)
        FROM entries
        WHERE businessId = :businessId
          AND occurredDate LIKE :yearMonth || '%'
          AND type = '${EntryType.INCOME}'
        """
    )
    suspend fun getIncomeSumByMonth(businessId: Long, yearMonth: String): Long?

    @Query(
        """
        SELECT SUM(amount)
        FROM entries
        WHERE businessId = :businessId
          AND occurredDate LIKE :yearMonth || '%'
          AND type = '${EntryType.EXPENSE}'
        """
    )
    suspend fun getExpenseSumByMonth(businessId: Long, yearMonth: String): Long?

    @Query(
        """
        SELECT
            occurredDate AS date,
            SUM(CASE WHEN type = '${EntryType.INCOME}' THEN amount ELSE 0 END) AS incomeSum,
            SUM(CASE WHEN type = '${EntryType.EXPENSE}' THEN amount ELSE 0 END) AS expenseSum
        FROM entries
        WHERE businessId = :businessId
          AND occurredDate LIKE :yearMonth || '%'
        GROUP BY occurredDate
        ORDER BY occurredDate ASC
        """
    )
    suspend fun getDailySumsForMonth(businessId: Long, yearMonth: String): List<DailySumRow>

    @Query(
        """
        SELECT
            c.id AS categoryId,
            c.name AS categoryName,
            SUM(e.amount) AS sum
        FROM entries e
        INNER JOIN categories c ON c.id = e.categoryId
        WHERE e.businessId = :businessId
          AND e.type = '${EntryType.EXPENSE}'
          AND e.occurredDate LIKE :yearMonth || '%'
        GROUP BY c.id, c.name
        ORDER BY sum DESC, c.name ASC
        """
    )
    suspend fun getExpenseSumsByCategoryForMonth(
        businessId: Long,
        yearMonth: String,
    ): List<CategorySumRow>
}
