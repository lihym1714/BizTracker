package com.biztracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: Category): Long

    @Update
    suspend fun update(category: Category)

    @Delete
    suspend fun delete(category: Category)

    @Query(
        """
        SELECT *
        FROM categories
        WHERE businessId = :businessId
          AND type = :type
        ORDER BY name ASC
        """
    )
    fun getAllByBusiness(businessId: Long, type: String): Flow<List<Category>>

    @Query(
        """
        SELECT COUNT(*)
        FROM categories
        WHERE businessId = :businessId
          AND type = :type
          AND isCustom = 1
        """
    )
    fun countCustomCategories(businessId: Long, type: String): Int
}
