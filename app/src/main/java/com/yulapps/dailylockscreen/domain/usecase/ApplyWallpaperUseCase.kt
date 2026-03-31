package com.yulapps.dailylockscreen.domain.usecase

import android.content.Context
import com.yulapps.dailylockscreen.data.SettingsRepository
import com.yulapps.dailylockscreen.data.WallpaperApplier
import com.yulapps.dailylockscreen.domain.model.AppSettings
import kotlinx.coroutines.flow.first

class ApplyWallpaperUseCase(
    private val settingsRepository: SettingsRepository,
    private val wallpaperApplier: WallpaperApplier,
    private val generateWallpaperUseCase: GenerateWallpaperUseCase,
) {
    suspend fun execute(
        context: Context,
        preferredQuote: String? = null,
        settingsOverride: AppSettings? = null,
    ): Result<String?> {
        return runCatching {
            val settings = settingsOverride ?: settingsRepository.settings.first()
            val generatedWallpaper = generateWallpaperUseCase.generate(
                context = context,
                settings = settings,
                preferredQuote = preferredQuote,
            )

            wallpaperApplier.applyLockScreenWallpaper(generatedWallpaper.bitmap)
            settingsRepository.recordWallpaperApplied(
                summary = generatedWallpaper.summary,
                quote = generatedWallpaper.quote,
            )
            generatedWallpaper.quote
        }.onFailure { throwable ->
            settingsRepository.recordWallpaperFailure(
                throwable.message ?: "Could not apply wallpaper.",
            )
        }
    }
}
