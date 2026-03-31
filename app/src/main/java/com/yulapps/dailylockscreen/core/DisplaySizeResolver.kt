package com.yulapps.dailylockscreen.core

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.util.Size
import android.view.WindowManager

class DisplaySizeResolver {
    fun resolve(context: Context): Size {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val desiredWidth = wallpaperManager.desiredMinimumWidth
        val desiredHeight = wallpaperManager.desiredMinimumHeight

        if (desiredWidth > 0 && desiredHeight > 0) {
            return Size(desiredWidth, desiredHeight)
        }

        val windowManager = context.getSystemService(WindowManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && windowManager != null) {
            val bounds = windowManager.currentWindowMetrics.bounds
            return Size(
                bounds.width().coerceAtLeast(720),
                bounds.height().coerceAtLeast(1280),
            )
        }

        val metrics = context.resources.displayMetrics
        return Size(
            metrics.widthPixels.coerceAtLeast(720),
            metrics.heightPixels.coerceAtLeast(1280),
        )
    }
}
