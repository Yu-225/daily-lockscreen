package com.yulapps.dailylockscreen.domain.usecase

import com.yulapps.dailylockscreen.data.SettingsRepository
import com.yulapps.dailylockscreen.data.WallpaperScheduler
import kotlinx.coroutines.flow.first

class ScheduleWallpaperUpdateUseCase(
    private val settingsRepository: SettingsRepository,
    private val wallpaperScheduler: WallpaperScheduler,
) {
    suspend fun sync() {
        val settings = settingsRepository.settings.first()
        if (settings.autoUpdate) {
            wallpaperScheduler.schedule(settings.updateInterval)
            settingsRepository.setLastScheduledAt(System.currentTimeMillis())
            settingsRepository.recordInfo("Auto update active every ${settings.updateInterval.label}.")
        } else {
            wallpaperScheduler.cancel()
            settingsRepository.recordInfo("Auto update paused.")
        }
    }
}
