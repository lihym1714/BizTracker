package com.biztracker.data.repository

import com.biztracker.data.local.Business
import com.biztracker.data.local.BusinessDao
import com.biztracker.domain.DateUtils

class BusinessRepository(
    private val businessDao: BusinessDao,
) {
    suspend fun getOrCreateDefaultBusiness(): Business {
        val existing = businessDao.getFirst()
        if (existing != null) {
            return existing
        }

        val defaultBusiness = Business(
            name = DEFAULT_BUSINESS_NAME,
            createdDate = DateUtils.today(),
        )
        val id = businessDao.insert(defaultBusiness)
        return defaultBusiness.copy(id = id)
    }

    private companion object {
        const val DEFAULT_BUSINESS_NAME = "My Business"
    }
}
