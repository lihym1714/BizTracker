package com.biztracker.domain

object EntryNoteCodec {
    private const val LEGACY_PAYMENT_PREFIX = "payment:"
    private const val NEW_PAYMENT_SEPARATOR = " - "

    fun split(
        note: String?,
        defaultPaymentMethod: String,
    ): Pair<String, String> {
        if (note.isNullOrBlank()) {
            return defaultPaymentMethod to ""
        }

        if (note.startsWith(LEGACY_PAYMENT_PREFIX)) {
            val lines = note.lines()
            val payment = lines.first()
                .removePrefix(LEGACY_PAYMENT_PREFIX)
                .ifBlank { defaultPaymentMethod }
            val memo = lines.drop(1).joinToString("\n")
            return payment to memo
        }

        val parts = note.split(NEW_PAYMENT_SEPARATOR, limit = 2)
        return if (parts.size == 2) {
            val payment = parts[0].trim().ifBlank { defaultPaymentMethod }
            val memo = parts[1].trim()
            payment to memo
        } else {
            defaultPaymentMethod to note
        }
    }

    fun merge(
        paymentMethod: String,
        memo: String,
    ): String {
        val cleanMemo = memo.trim()
        return if (cleanMemo.isBlank()) {
            paymentMethod
        } else {
            "$paymentMethod$NEW_PAYMENT_SEPARATOR$cleanMemo"
        }
    }
}
