package com.yulapps.dailylockscreen.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WallpaperApplier(
    context: Context,
) {
    private val wallpaperManager = WallpaperManager.getInstance(context)

    @Suppress("DEPRECATION")
    suspend fun applyLockScreenWallpaper(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
        }
    }
}
