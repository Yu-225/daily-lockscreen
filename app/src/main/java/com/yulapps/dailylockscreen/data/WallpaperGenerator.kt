package com.yulapps.dailylockscreen.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.text.LineBreaker
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Size
import com.yulapps.dailylockscreen.domain.model.AppSettings
import com.yulapps.dailylockscreen.domain.model.BackgroundImageScaleMode
import com.yulapps.dailylockscreen.domain.model.TextAlignmentPreset
import com.yulapps.dailylockscreen.domain.model.TextFontPreset
import com.yulapps.dailylockscreen.domain.model.TextHorizontalPosition
import com.yulapps.dailylockscreen.domain.model.TextVerticalPosition
import com.yulapps.dailylockscreen.domain.model.WallpaperMode
import kotlin.math.min
import kotlin.math.roundToInt

class WallpaperGenerator {
    fun generate(
        context: Context,
        size: Size,
        settings: AppSettings,
        quote: String?,
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawBackground(context, canvas, size, settings)

        if (settings.wallpaperMode == WallpaperMode.BLACK_WITH_QUOTE && !quote.isNullOrBlank()) {
            drawQuote(canvas, size, quote, settings)
        }

        return bitmap
    }

    private fun drawBackground(
        context: Context,
        canvas: Canvas,
        size: Size,
        settings: AppSettings,
    ) {
        val imageUri = settings.backgroundImageUri
        if (imageUri.isNullOrBlank()) {
            canvas.drawColor(Color.BLACK)
            return
        }

        val backgroundBitmap = decodeBitmap(context, imageUri, size)
        val destination = computeDestinationRect(
            sourceWidth = backgroundBitmap.width,
            sourceHeight = backgroundBitmap.height,
            targetWidth = size.width,
            targetHeight = size.height,
            scaleMode = settings.backgroundImageScaleMode,
        )

        canvas.drawColor(Color.BLACK)
        canvas.drawBitmap(backgroundBitmap, null, destination, Paint(Paint.FILTER_BITMAP_FLAG))

        val overlayAlpha = (255 * (settings.backgroundOverlayPercent / 100f)).roundToInt().coerceIn(0, 255)
        if (overlayAlpha > 0) {
            canvas.drawColor(Color.argb(overlayAlpha, 0, 0, 0))
        }
    }

    private fun drawQuote(
        canvas: Canvas,
        size: Size,
        quote: String,
        settings: AppSettings,
    ) {
        val blockWidth = (size.width * 0.74f).roundToInt()
        val horizontalPadding = size.width * 0.1f
        val verticalPadding = size.height * 0.11f
        val maxTextHeight = (size.height * 0.6f).roundToInt()
        val safeQuote = softenLongWords(quote)
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = toAndroidColor(settings.textColorPreset.argb, settings.textOpacityPercent)
            textSize = min(size.width, size.height) * 0.067f * settings.textSizePreset.scaleMultiplier
            typeface = resolveTypeface(settings.textFontPreset, settings.isTextBold)
            setShadowLayer(size.height * 0.008f, 0f, size.height * 0.003f, Color.argb(170, 0, 0, 0))
        }

        var layout = buildLayout(safeQuote, paint, blockWidth, settings.textAlignmentPreset)
        var attempts = 0
        while (layout.height > maxTextHeight && attempts < 12) {
            paint.textSize *= 0.92f
            layout = buildLayout(safeQuote, paint, blockWidth, settings.textAlignmentPreset)
            attempts += 1
        }

        val left = when (settings.textHorizontalPosition) {
            TextHorizontalPosition.LEFT -> horizontalPadding
            TextHorizontalPosition.CENTER -> (size.width - blockWidth) / 2f
            TextHorizontalPosition.RIGHT -> size.width - blockWidth - horizontalPadding
        }.coerceIn(24f, (size.width - blockWidth - 24).toFloat())

        val top = when (settings.textVerticalPosition) {
            TextVerticalPosition.TOP -> verticalPadding
            TextVerticalPosition.CENTER -> (size.height - layout.height) / 2f
            TextVerticalPosition.BOTTOM -> size.height - layout.height - verticalPadding
        }.coerceIn(24f, (size.height - layout.height - 24).toFloat())

        canvas.save()
        canvas.translate(left, top)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun buildLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        alignmentPreset: TextAlignmentPreset,
    ): StaticLayout {
        val alignment = when (alignmentPreset) {
            TextAlignmentPreset.LEFT -> Layout.Alignment.ALIGN_NORMAL
            TextAlignmentPreset.CENTER -> Layout.Alignment.ALIGN_CENTER
            TextAlignmentPreset.RIGHT -> Layout.Alignment.ALIGN_OPPOSITE
        }
        return StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(alignment)
            .setIncludePad(false)
            .setBreakStrategy(LineBreaker.BREAK_STRATEGY_BALANCED)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL_FAST)
            .build()
    }

    private fun resolveTypeface(fontPreset: TextFontPreset, isBold: Boolean): Typeface {
        val style = if (isBold) Typeface.BOLD else Typeface.NORMAL
        return when (fontPreset) {
            TextFontPreset.SANS -> Typeface.create(Typeface.SANS_SERIF, style)
            TextFontPreset.SERIF -> Typeface.create(Typeface.SERIF, style)
            TextFontPreset.MONO -> Typeface.create(Typeface.MONOSPACE, style)
        }
    }

    private fun softenLongWords(text: String): String {
        return text.replace(Regex("(\\S{10})(?=\\S)"), "$1\u200B")
    }

    private fun toAndroidColor(argb: Long, opacityPercent: Int): Int {
        val baseAlpha = ((argb shr 24) and 0xFF).toInt()
        val alpha = (baseAlpha * (opacityPercent.coerceIn(40, 100) / 100f)).roundToInt()
        val red = ((argb shr 16) and 0xFF).toInt()
        val green = ((argb shr 8) and 0xFF).toInt()
        val blue = (argb and 0xFF).toInt()
        return Color.argb(alpha, red, green, blue)
    }

    private fun computeDestinationRect(
        sourceWidth: Int,
        sourceHeight: Int,
        targetWidth: Int,
        targetHeight: Int,
        scaleMode: BackgroundImageScaleMode,
    ): RectF {
        if (scaleMode == BackgroundImageScaleMode.FILL) {
            return RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat())
        }

        val sourceAspect = sourceWidth / sourceHeight.toFloat()
        val targetAspect = targetWidth / targetHeight.toFloat()
        val scale = when (scaleMode) {
            BackgroundImageScaleMode.FIT -> min(targetWidth / sourceWidth.toFloat(), targetHeight / sourceHeight.toFloat())
            BackgroundImageScaleMode.CROP -> maxOf(targetWidth / sourceWidth.toFloat(), targetHeight / sourceHeight.toFloat())
            BackgroundImageScaleMode.FILL -> 1f
        }
        val scaledWidth = sourceWidth * scale
        val scaledHeight = sourceHeight * scale
        val left = (targetWidth - scaledWidth) / 2f
        val top = (targetHeight - scaledHeight) / 2f
        return if (scaleMode == BackgroundImageScaleMode.FIT && sourceAspect == targetAspect) {
            RectF(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat())
        } else {
            RectF(left, top, left + scaledWidth, top + scaledHeight)
        }
    }

    private fun decodeBitmap(
        context: Context,
        uriString: String,
        targetSize: Size,
    ): Bitmap {
        val uri = Uri.parse(uriString)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        } ?: error("Unable to open image stream")

        val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, targetSize)
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } ?: error("Unable to decode image")
    }

    private fun calculateSampleSize(
        sourceWidth: Int,
        sourceHeight: Int,
        targetSize: Size,
    ): Int {
        var sample = 1
        if (sourceWidth <= 0 || sourceHeight <= 0) return sample

        var currentWidth = sourceWidth
        var currentHeight = sourceHeight
        val maxWidth = targetSize.width * 2
        val maxHeight = targetSize.height * 2
        while (currentWidth / 2 >= maxWidth && currentHeight / 2 >= maxHeight) {
            currentWidth /= 2
            currentHeight /= 2
            sample *= 2
        }
        return sample.coerceAtLeast(1)
    }
}

