package com.kazumaproject.tenkey.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * [atok-style] 絵文字キーから指を離さずにスライドしたときに、キーボード全体に重ねて出す絵文字の一覧。
 * タッチは TenKey 側で受け、画面座標からマスを求めて [setHighlight] / [itemAtCell] を呼ぶ。
 * 左下のマス（絵文字キーの位置）は「×」で、そこで指を離すと入力しない。
 */
class AtokEmojiSwipePanel(context: Context) : View(context) {

    val columns = 6
    val rows = 4
    private val cancelCell = (rows - 1) * columns

    private var items: List<String> = emptyList()
    private var highlightCell = -1

    private val isDark: Boolean
        get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val cellRect = RectF()

    /** 最大 rows*columns-1 個（×のマスを除く） */
    fun setItems(list: List<String>) {
        items = list
        invalidate()
    }

    fun hasItems(): Boolean = items.isNotEmpty()

    fun setHighlight(cell: Int) {
        if (cell != highlightCell) {
            highlightCell = cell
            invalidate()
        }
    }

    /** 画面座標からマス番号。範囲外は -1 */
    fun cellAtRaw(rawX: Float, rawY: Float): Int {
        if (width <= 0 || height <= 0) return -1
        val loc = IntArray(2)
        getLocationOnScreen(loc)
        val x = rawX - loc[0]
        val y = rawY - loc[1]
        if (x < 0 || y < 0 || x >= width || y >= height) return -1
        val c = (x / (width / columns.toFloat())).toInt().coerceIn(0, columns - 1)
        val r = (y / (height / rows.toFloat())).toInt().coerceIn(0, rows - 1)
        return r * columns + c
    }

    /** マスの絵文字。×のマス・空きマス・範囲外は null */
    fun itemAtCell(cell: Int): String? {
        if (cell < 0 || cell == cancelCell) return null
        val index = if (cell > cancelCell) cell - 1 else cell
        return items.getOrNull(index)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        bgPaint.color = if (isDark) 0xF2202124.toInt() else 0xF2F1F3F4.toInt()
        highlightPaint.color = 0x663D7EFF
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        val cw = width / columns.toFloat()
        val ch = height / rows.toFloat()
        for (cell in 0 until rows * columns) {
            val r = cell / columns
            val c = cell % columns
            cellRect.set(c * cw, r * ch, (c + 1) * cw, (r + 1) * ch)
            if (cell == highlightCell) {
                val inset = 3f * density
                cellRect.inset(inset, inset)
                canvas.drawRoundRect(cellRect, 10f * density, 10f * density, highlightPaint)
                cellRect.set(c * cw, r * ch, (c + 1) * cw, (r + 1) * ch)
            }
            val label = if (cell == cancelCell) "×" else itemAtCell(cell) ?: continue
            textPaint.textSize = minOf(cw, ch) * (if (cell == cancelCell) 0.35f else 0.5f)
            textPaint.color = if (isDark) 0xFFE8EAED.toInt() else 0xFF3C4043.toInt()
            val y = cellRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(label, cellRect.centerX(), y, textPaint)
        }
    }
}
