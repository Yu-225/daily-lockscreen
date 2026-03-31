package com.yulapps.dailylockscreen.domain.model

data class AppSettings(
    val autoUpdate: Boolean = false,
    val updateInterval: UpdateInterval = UpdateInterval.FOUR_HOURS,
    val wallpaperMode: WallpaperMode = WallpaperMode.BLACK_WITH_QUOTE,
    val quoteSourceMode: QuoteSourceMode = QuoteSourceMode.MIXED,
    val customQuotesInput: String = "",
    val backgroundImageUri: String? = null,
    val backgroundImageScaleMode: BackgroundImageScaleMode = BackgroundImageScaleMode.CROP,
    val backgroundOverlayPercent: Int = 42,
    val textSizePreset: TextSizePreset = TextSizePreset.MEDIUM,
    val textColorPreset: TextColorPreset = TextColorPreset.WHITE,
    val textAlignmentPreset: TextAlignmentPreset = TextAlignmentPreset.CENTER,
    val textHorizontalPosition: TextHorizontalPosition = TextHorizontalPosition.CENTER,
    val textVerticalPosition: TextVerticalPosition = TextVerticalPosition.CENTER,
    val textFontPreset: TextFontPreset = TextFontPreset.SANS,
    val isTextBold: Boolean = false,
    val textOpacityPercent: Int = 100,
    val lastAppliedQuote: String? = null,
    val lastAppliedSummary: String? = null,
    val lastUpdatedAtMillis: Long? = null,
    val lastScheduledAtMillis: Long? = null,
    val updateLogs: List<UpdateLogEntry> = emptyList(),
) {
    val customQuotes: List<String>
        get() = customQuotesInput
            .lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    val hasCustomImage: Boolean
        get() = !backgroundImageUri.isNullOrBlank()
}
