package com.yulapps.dailylockscreen.data

import com.yulapps.dailylockscreen.domain.model.QuoteSourceMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuoteRepositoryTest {
    @Test
    fun `returns another quote when last quote exists in custom pool`() {
        val repository = QuoteRepository(listOf("Built in"))

        val result = repository.getRandomQuote(
            lastQuote = "A",
            quoteSourceMode = QuoteSourceMode.CUSTOM_ONLY,
            customQuotesInput = "A\nB",
        )

        assertEquals("B", result)
    }

    @Test
    fun `returns null when custom only mode has no quotes`() {
        val repository = QuoteRepository(emptyList())

        val result = repository.getRandomQuote(
            lastQuote = null,
            quoteSourceMode = QuoteSourceMode.CUSTOM_ONLY,
            customQuotesInput = "   \n\n",
        )

        assertNull(result)
    }

    @Test
    fun `sanitizes duplicate and blank custom quote lines`() {
        val repository = QuoteRepository()

        val result = repository.sanitizeCustomQuoteInput(" First \n\nSecond\nFirst\n  Second  ")

        assertEquals("First\nSecond", result)
    }
}
