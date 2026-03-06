package com.biztracker.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object EntryType {
    const val INCOME = "INCOME"
    const val EXPENSE = "EXPENSE"
}

@Entity(tableName = "businesses")
data class Business(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdDate: String,
)

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = Business::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["businessId", "type"]),
    ],
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val name: String,
    val type: String,
    val isCustom: Boolean = true,
)

@Entity(
    tableName = "entries",
    foreignKeys = [
        ForeignKey(
            entity = Business::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["businessId", "occurredDate"]),
        Index(value = ["categoryId"]),
    ],
)
data class Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val businessId: Long,
    val categoryId: Long?,
    val type: String,
    val amount: Long,
    val occurredDate: String,
    val note: String? = null,
)

data class DailySumRow(
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "incomeSum")
    val incomeSum: Long,
    @ColumnInfo(name = "expenseSum")
    val expenseSum: Long,
)

data class CategorySumRow(
    @ColumnInfo(name = "categoryId")
    val categoryId: Long,
    @ColumnInfo(name = "categoryName")
    val categoryName: String,
    @ColumnInfo(name = "sum")
    val sum: Long,
)

data class EntryHistoryRow(
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "type")
    val type: String,
    @ColumnInfo(name = "amount")
    val amount: Long,
    @ColumnInfo(name = "occurredDate")
    val occurredDate: String,
    @ColumnInfo(name = "note")
    val note: String?,
    @ColumnInfo(name = "categoryName")
    val categoryName: String?,
)
