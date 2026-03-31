package com.yulapps.dailylockscreen.domain.usecase

import android.content.Context
import com.yulapps.dailylockscreen.core.DisplaySizeResolver
import com.yulapps.dailylockscreen.data.WallpaperGenerator
import com.yulapps.dailylockscreen.domain.model.AppSettings
import com.yulapps.dailylockscreen.domain.model.GeneratedWallpaper
import com.yulapps.dailylockscreen.domain.model.WallpaperMode

class GenerateWallpaperUseCase(
    private val displaySizeResolver: DisplaySizeResolver,
    private val wallpaperGenerator: WallpaperGenerator,
    private val getRandomQuoteUseCase: GetRandomQuoteUseCase,
) {
    fun generate(
        context: Context,
        settings: AppSettings,
        preferredQuote: String? = null,
    ): GeneratedWallpaper {
        val quote = when (settings.wallpaperMode) {
            WallpaperMode.BLACK_ONLY -> null
            WallpaperMode.BLACK_WITH_QUOTE -> preferredQuote ?: getRandomQuoteUseCase(
                lastQuote = settings.lastAppliedQuote,
                quoteSourceMode = settings.quoteSourceMode,
                customQuotesInput = settings.customQuotesInput,
            )
        }

        val size = displaySizeResolver.resolve(context)
        val bitmap = wallpaperGenerator.generate(
            context = context,
            size = size,
            settings = settings,
            quote = quote,
        )
        return GeneratedWallpaper(
            bitmap = bitmap,
            quote = quote,
            summary = buildSummary(settings, quote),
        )
    }

    private fun buildSummary(settings: AppSettings, quote: String?): String {
        val backgroundLabel = if (settings.hasCustomImage) {
            "Custom image"
        } else {
            "Black background"
        }
        val textLabel = when (settings.wallpaperMode) {
            WallpaperMode.BLACK_ONLY -> "No quote"
            WallpaperMode.BLACK_WITH_QUOTE -> quote?.take(56) ?: "No quote available"
        }
        return "$backgroundLabel • $textLabel"
    }
}
