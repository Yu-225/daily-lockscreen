package com.yulapps.dailylockscreen.data

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.yulapps.dailylockscreen.domain.model.UpdateInterval
import com.yulapps.dailylockscreen.worker.WallpaperUpdateWorker
import kotlinx.coroutines.flow.Flow

class WallpaperScheduler(
    private val context: Context,
) {
    fun schedule(interval: UpdateInterval) {
        val request = PeriodicWorkRequestBuilder<WallpaperUpdateWorker>(
            interval.repeatInterval,
            interval.timeUnit,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    fun observeWorkInfo(): Flow<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME)
            .asFlow()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "daily_lockscreen_periodic_update"
    }
}
