package com.kazumaproject.tenkey.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

/**
 * [atok-style] ATOK風のフリックガイド（丸い表示）。キーの1段上に出して、指で隠れないようにする。
 * 中央・左・上・右・下の5つの文字を表示し、選択中の方向を塗りつぶす。
 */
class AtokFlickRingView(context: Context) : View(context) {

    enum class Direction { CENTER, LEFT, TOP, RIGHT, BOTTOM }

    private var labels: Map<Direction, String> = emptyMap()
    private var selected: Direction = Direction.CENTER

    private val isDark: Boolean
        get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val selectedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    fun setLabels(labels: Map<Direction, String>) {
        this.labels = labels
        invalidate()
    }

    fun setSelected(direction: Direction) {
        if (direction != selected) {
            selected = direction
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = minOf(width, height).toFloat()
        if (size <= 0f) return
        val density = resources.displayMetrics.density
        val cx = width / 2f
        val cy = height / 2f
        val radius = size / 2f - 2f * density
        ringPaint.color = if (isDark) 0xF23C4043.toInt() else 0xF2FFFFFF.toInt()
        borderPaint.color = if (isDark) 0xFF5F6368.toInt() else 0xFFBDC1C6.toInt()
        borderPaint.strokeWidth = 4f * density
        selectedPaint.color = if (isDark) 0xFFE8EAED.toInt() else 0xFF202124.toInt()
        canvas.drawCircle(cx, cy, radius, ringPaint)
        canvas.drawCircle(cx, cy, radius, borderPaint)

        val offset = radius * 0.62f
        val slotRadius = radius * 0.3f
        val positions = mapOf(
            Direction.CENTER to (cx to cy),
            Direction.LEFT to (cx - offset to cy),
            Direction.TOP to (cx to cy - offset),
            Direction.RIGHT to (cx + offset to cy),
            Direction.BOTTOM to (cx to cy + offset),
        )
        for ((direction, pos) in positions) {
            val label = labels[direction].orEmpty()
            val isSelected = direction == selected
            if (isSelected) canvas.drawCircle(pos.first, pos.second, slotRadius, selectedPaint)
            if (label.isEmpty()) continue
            textPaint.textSize = if (label.length >= 3) slotRadius * 0.75f else slotRadius * 1.1f
            textPaint.color = when {
                isSelected -> if (isDark) 0xFF202124.toInt() else 0xFFFFFFFF.toInt()
                else -> if (isDark) 0xFFE8EAED.toInt() else 0xFF3C4043.toInt()
            }
            val y = pos.second - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(label, pos.first, y, textPaint)
        }
    }
}
