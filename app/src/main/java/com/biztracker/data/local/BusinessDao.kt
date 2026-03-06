package com.biztracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface BusinessDao {
    @Insert
    suspend fun insert(business: Business): Long

    @Query("SELECT * FROM businesses ORDER BY id ASC LIMIT 1")
    suspend fun getFirst(): Business?
}
