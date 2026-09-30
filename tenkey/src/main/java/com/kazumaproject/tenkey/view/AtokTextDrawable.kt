package com.kazumaproject.tenkey.view

import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.Drawable

/**
 * [atok-style] キーに文字ラベル（「カナ英数」「後変換」）を表示するための Drawable。
 * ImageView の imageTint を文字色として使う。
 */
class AtokTextDrawable(
    private val text: String,
    private val textSizePx: Float,
    private val fallbackColor: Int,
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private var tint: ColorStateList? = null
    private var alphaValue: Int = 255

    override fun draw(canvas: Canvas) {
        val b = bounds
        if (b.isEmpty) return
        paint.color = tint?.getColorForState(state, tint?.defaultColor ?: fallbackColor)
            ?: fallbackColor
        paint.alpha = alphaValue
        var size = textSizePx
        paint.textSize = size
        val maxWidth = b.width() * 0.9f
        while (paint.measureText(text) > maxWidth && size > 6f) {
            size -= 1f
            paint.textSize = size
        }
        val y = b.exactCenterY() - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(text, b.exactCenterX(), y, paint)
    }

    override fun setTintList(tint: ColorStateList?) {
        this.tint = tint
        invalidateSelf()
    }

    override fun isStateful(): Boolean = tint?.isStateful == true

    override fun onStateChange(state: IntArray): Boolean {
        invalidateSelf()
        return true
    }

    override fun setAlpha(alpha: Int) {
        alphaValue = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
