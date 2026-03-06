package com.biztracker.domain

data class Summary(
    val income: Long,
    val expense: Long,
) {
    val profit: Long
        get() = income - expense
}

data class DailySum(
    val date: String,
    val income: Long,
    val expense: Long,
) {
    val profit: Long
        get() = income - expense
}

data class CategorySum(
    val categoryId: Long,
    val categoryName: String,
    val sum: Long,
)

data class EntryHistory(
    val id: Long,
    val type: String,
    val amount: Long,
    val occurredDate: String,
    val categoryName: String,
    val paymentMethod: String,
    val memo: String,
)
