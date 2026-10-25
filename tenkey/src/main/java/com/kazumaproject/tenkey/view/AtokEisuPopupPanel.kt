package com.kazumaproject.tenkey.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * [atok-style] ATOK風のカナ英数ポップアップ。キーボード全体に重ねて表示する。
 * 上段に「半角」「全角」のタブと「×」、その下に候補を3列で並べる。
 * タッチは TenKey 側で受け、[hitAtRaw] で押された場所を判定する。
 */
class AtokEisuPopupPanel(context: Context) : View(context) {

    sealed class Hit {
        data class Tab(val index: Int) : Hit()
        data object Close : Hit()
        data class Candidate(val text: String) : Hit()
        data object None : Hit()
    }

    private val columns = 3
    private val rows = 4
    private var half: List<String> = emptyList()
    private var full: List<String> = emptyList()
    var selectedTab: Int = 0
        private set

    private val isDark: Boolean
        get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tabPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cellPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    fun setCandidates(half: List<String>, full: List<String>) {
        this.half = half
        this.full = full
        selectedTab = 0
        invalidate()
    }

    fun selectTab(index: Int) {
        selectedTab = index.coerceIn(0, 1)
        invalidate()
    }

    private val current: List<String> get() = if (selectedTab == 0) half else full
    private val tabHeight: Float get() = height / (rows + 1f)

    fun hitAtRaw(rawX: Float, rawY: Float): Hit {
        if (width <= 0 || height <= 0) return Hit.None
        val loc = IntArray(2)
        getLocationOnScreen(loc)
        val x = rawX - loc[0]
        val y = rawY - loc[1]
        if (x < 0 || y < 0 || x >= width || y >= height) return Hit.None
        if (y < tabHeight) {
            return when ((x / (width / 3f)).toInt().coerceIn(0, 2)) {
                0 -> Hit.Tab(0)
                1 -> Hit.Tab(1)
                else -> Hit.Close
            }
        }
        val c = (x / (width / columns.toFloat())).toInt().coerceIn(0, columns - 1)
        val r = ((y - tabHeight) / ((height - tabHeight) / rows)).toInt().coerceIn(0, rows - 1)
        val text = current.getOrNull(r * columns + c) ?: return Hit.None
        return Hit.Candidate(text)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return
        val density = resources.displayMetrics.density
        val textColor = if (isDark) 0xFFE8EAED.toInt() else 0xFF202124.toInt()
        bgPaint.color = if (isDark) 0xFA202124.toInt() else 0xFAF8F9FA.toInt()
        tabPaint.color = 0xFF3D7EFF.toInt()
        cellPaint.color = if (isDark) 0xFF3C4043.toInt() else 0xFFFFFFFF.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // タブ
        val tw = width / 3f
        val labels = listOf("半角", "全角", "×")
        for (i in 0..2) {
            val rect = RectF(i * tw, 0f, (i + 1) * tw, tabHeight).apply { inset(4f * density, 4f * density) }
            if (i == selectedTab) canvas.drawRoundRect(rect, 8f * density, 8f * density, tabPaint)
            textPaint.color = if (i == selectedTab) 0xFFFFFFFF.toInt() else textColor
            textPaint.textSize = tabHeight * 0.38f
            val y = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(labels[i], rect.centerX(), y, textPaint)
        }

        // 候補
        val cw = width / columns.toFloat()
        val ch = (height - tabHeight) / rows
        val items = current
        for (i in 0 until minOf(items.size, columns * rows)) {
            val r = i / columns
            val c = i % columns
            val rect = RectF(c * cw, tabHeight + r * ch, (c + 1) * cw, tabHeight + (r + 1) * ch)
                .apply { inset(3f * density, 3f * density) }
            canvas.drawRoundRect(rect, 8f * density, 8f * density, cellPaint)
            textPaint.color = textColor
            var size = ch * 0.38f
            textPaint.textSize = size
            while (textPaint.measureText(items[i]) > rect.width() * 0.9f && size > 8f) {
                size -= 1f
                textPaint.textSize = size
            }
            val y = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(items[i], rect.centerX(), y, textPaint)
        }
    }
}
