package com.biztracker.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class EntryNoteCodecTest {
    @Test
    fun split_withNewFormat_returnsPaymentAndMemo() {
        val result = EntryNoteCodec.split("CARD - Taxi fare", defaultPaymentMethod = Constants.PAYMENT_CASH)
        assertEquals(Constants.PAYMENT_CARD, result.first)
        assertEquals("Taxi fare", result.second)
    }

    @Test
    fun split_withPlainMemo_usesDefaultPayment() {
        val result = EntryNoteCodec.split("Plain memo", defaultPaymentMethod = Constants.PAYMENT_CASH)
        assertEquals(Constants.PAYMENT_CASH, result.first)
        assertEquals("Plain memo", result.second)
    }

    @Test
    fun merge_buildsExpectedFormat() {
        val merged = EntryNoteCodec.merge(Constants.PAYMENT_TRANSFER, "Wire settlement")
        assertEquals("TRANSFER - Wire settlement", merged)
    }
}
