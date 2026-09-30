package com.kazumaproject.tenkey.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.widget.TextView

/**
 * [atok-style] かなキーの左下に数字、右下に英字（例: 2 / ABC）を重ねて描く透明なレイヤー。
 * キーと同じ親（TenKey）に全面で重ね、各キーの位置を毎回読んで描く。
 */
class AtokKeyHintLayer(context: Context) : View(context) {

    private var hints: List<Triple<TextView, String, String>> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun setHints(list: List<Triple<TextView, String, String>>) {
        hints = list
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        paint.textSize = 10f * resources.displayMetrics.scaledDensity
        val padX = 6f * density
        val padBottom = 5f * density
        for ((key, number, letters) in hints) {
            if (key.visibility != VISIBLE || key.width <= 0) continue
            paint.color = key.currentTextColor
            paint.alpha = 190
            val baseline = key.bottom - padBottom
            if (number.isNotEmpty()) {
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(number, key.left + padX, baseline, paint)
            }
            if (letters.isNotEmpty()) {
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(letters, key.right - padX, baseline, paint)
            }
        }
    }
}
