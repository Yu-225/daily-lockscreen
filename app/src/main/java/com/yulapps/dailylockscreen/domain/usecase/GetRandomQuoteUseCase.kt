package com.yulapps.dailylockscreen.domain.usecase

import com.yulapps.dailylockscreen.data.QuoteRepository
import com.yulapps.dailylockscreen.domain.model.QuoteSourceMode

class GetRandomQuoteUseCase(
    private val quoteRepository: QuoteRepository,
) {
    operator fun invoke(
        lastQuote: String?,
        quoteSourceMode: QuoteSourceMode,
        customQuotesInput: String,
    ): String? {
        return quoteRepository.getRandomQuote(
            lastQuote = lastQuote,
            quoteSourceMode = quoteSourceMode,
            customQuotesInput = customQuotesInput,
        )
    }
}
