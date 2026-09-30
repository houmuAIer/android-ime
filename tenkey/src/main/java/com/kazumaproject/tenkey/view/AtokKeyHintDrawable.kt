package com.kazumaproject.tenkey.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.widget.TextView

/**
 * [atok-style] かなキーの左下に数字、右下に英字（例: 2 / ABC）を小さく重ねて表示する。
 * 文字色はキーの文字色に合わせる。
 */
class AtokKeyHintDrawable(
    private val key: TextView,
    private val numberText: String,
    private val letterText: String,
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var alphaValue = 255

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        val density = key.resources.displayMetrics.density
        paint.textSize = 10f * key.resources.displayMetrics.scaledDensity
        paint.color = key.currentTextColor
        paint.alpha = (alphaValue * 0.75f).toInt()
        val padX = 6f * density
        val baseline = b.bottom - 6f * density
        if (numberText.isNotEmpty()) {
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(numberText, b.left + padX, baseline, paint)
        }
        if (letterText.isNotEmpty()) {
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(letterText, b.right - padX, baseline, paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        alphaValue = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
