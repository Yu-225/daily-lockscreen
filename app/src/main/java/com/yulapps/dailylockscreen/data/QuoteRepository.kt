package com.yulapps.dailylockscreen.data

import com.yulapps.dailylockscreen.domain.model.QuoteSourceMode

class QuoteRepository(
    private val builtInQuotes: List<String> = DEFAULT_QUOTES,
) {
    fun sanitizeCustomQuoteInput(rawInput: String): String {
        return extractCustomQuotes(rawInput).joinToString(separator = "\n")
    }

    fun appendQuote(existingInput: String, newQuote: String): String {
        val merged = buildList {
            addAll(extractCustomQuotes(existingInput))
            add(newQuote.trim())
        }
        return merged
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(separator = "\n")
    }

    fun extractCustomQuotes(rawInput: String): List<String> {
        return rawInput
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun getRandomQuote(
        lastQuote: String?,
        quoteSourceMode: QuoteSourceMode,
        customQuotesInput: String,
    ): String? {
        val pool = when (quoteSourceMode) {
            QuoteSourceMode.BUILT_IN -> builtInQuotes
            QuoteSourceMode.CUSTOM_ONLY -> extractCustomQuotes(customQuotesInput)
            QuoteSourceMode.MIXED -> (builtInQuotes + extractCustomQuotes(customQuotesInput)).distinct()
        }

        if (pool.isEmpty()) {
            return null
        }

        if (pool.size == 1 || lastQuote.isNullOrBlank()) {
            return pool.random()
        }

        val filtered = pool.filterNot { it == lastQuote }
        return if (filtered.isNotEmpty()) filtered.random() else pool.random()
    }

    companion object {
        val DEFAULT_QUOTES = listOf(
            "Small steps every day build a life you trust.",
            "Show up first. Clarity usually follows.",
            "Progress beats perfection on ordinary days.",
            "Quiet discipline changes more than loud motivation.",
            "You do not need a perfect plan to start moving.",
            "Consistency is a calm kind of power.",
            "Make today simpler, then make it better.",
            "Done with care is better than delayed by doubt.",
            "Protect your focus and the work will compound.",
            "A better routine can feel like a better future.",
        )
    }
}
