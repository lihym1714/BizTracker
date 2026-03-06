package com.biztracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Business::class,
        Category::class,
        Entry::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao

    abstract fun categoryDao(): CategoryDao

    abstract fun entryDao(): EntryDao
}
