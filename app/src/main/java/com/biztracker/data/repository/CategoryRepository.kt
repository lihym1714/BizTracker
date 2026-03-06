package com.biztracker.data.repository

import com.biztracker.data.local.Category
import com.biztracker.data.local.CategoryDao
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao,
) {
    suspend fun addCategory(
        businessId: Long,
        name: String,
        type: String,
        isCustom: Boolean = true,
    ): Long {
        return categoryDao.insert(
            Category(
                businessId = businessId,
                name = name,
                type = type,
                isCustom = isCustom,
            )
        )
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.update(category)
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.delete(category)
    }

    fun categories(businessId: Long, type: String): Flow<List<Category>> {
        return categoryDao.getAllByBusiness(businessId, type)
    }

    suspend fun canAddCustomCategory(
        businessId: Long,
        type: String,
        isPro: Boolean,
    ): Boolean {
        if (isPro) {
            return true
        }
        return categoryDao.countCustomCategories(businessId, type) < FREE_CUSTOM_CATEGORY_LIMIT
    }

    private companion object {
        const val FREE_CUSTOM_CATEGORY_LIMIT = 5
    }
}
