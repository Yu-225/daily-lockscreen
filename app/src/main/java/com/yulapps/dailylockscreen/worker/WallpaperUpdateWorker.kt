package com.yulapps.dailylockscreen.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yulapps.dailylockscreen.core.DisplaySizeResolver
import com.yulapps.dailylockscreen.data.QuoteRepository
import com.yulapps.dailylockscreen.data.SettingsRepository
import com.yulapps.dailylockscreen.data.WallpaperApplier
import com.yulapps.dailylockscreen.data.WallpaperGenerator
import com.yulapps.dailylockscreen.domain.usecase.ApplyWallpaperUseCase
import com.yulapps.dailylockscreen.domain.usecase.GenerateWallpaperUseCase
import com.yulapps.dailylockscreen.domain.usecase.GetRandomQuoteUseCase
import kotlinx.coroutines.flow.first

class WallpaperUpdateWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    private val settingsRepository = SettingsRepository(appContext)
    private val applyWallpaperUseCase = ApplyWallpaperUseCase(
        settingsRepository = settingsRepository,
        wallpaperApplier = WallpaperApplier(appContext),
        generateWallpaperUseCase = GenerateWallpaperUseCase(
            displaySizeResolver = DisplaySizeResolver(),
            wallpaperGenerator = WallpaperGenerator(),
            getRandomQuoteUseCase = GetRandomQuoteUseCase(QuoteRepository()),
        ),
    )

    override suspend fun doWork(): Result {
        val settings = settingsRepository.settings.first()
        if (!settings.autoUpdate) {
            return Result.success()
        }

        return applyWallpaperUseCase.execute(applicationContext).fold(
            onSuccess = { Result.success() },
            onFailure = { throwable ->
                Log.e(TAG, "Failed to update lock-screen wallpaper", throwable)
                Result.retry()
            },
        )
    }

    private companion object {
        const val TAG = "WallpaperUpdateWorker"
    }
}
