package com.kazumaproject.tenkey.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * [atok-style] 絵文字キーから指を離さずにスライドしたときに、キーボード全体に重ねて出す一覧。
 * マスはキーと同じ5列×4段。上3段が絵文字、最下段は操作用で、
 * 左端（絵文字キーの位置）が「×」＝入力しない、その右が「絵文字一覧を開く」。
 * タッチは TenKey 側で受け、画面座標からマスを求めて [setHighlight] などを呼ぶ。
 */
class AtokEmojiSwipePanel(context: Context) : View(context) {

    val columns = 5
    val rows = 4
    private val emojiCellCount = (rows - 1) * columns
    private val cancelCell = (rows - 1) * columns
    private val emojiPageCell = cancelCell + 1

    private var items: List<String> = emptyList()
    private var highlightCell = -1

    private val isDark: Boolean
        get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val menuPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val cellRect = RectF()

    /** 絵文字は最大 15 個（上3段ぶん） */
    fun setItems(list: List<String>) {
        items = list
        invalidate()
    }

    fun setHighlight(cell: Int) {
        if (cell != highlightCell) {
            highlightCell = cell
            invalidate()
        }
    }

    fun isEmojiPageCell(cell: Int): Boolean = cell == emojiPageCell

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

    /** マスの絵文字。操作用の段・空きマス・範囲外は null */
    fun itemAtCell(cell: Int): String? {
        if (cell < 0 || cell >= emojiCellCount) return null
        return items.getOrNull(cell)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        bgPaint.color = if (isDark) 0xFA202124.toInt() else 0xFAF8F9FA.toInt()
        menuPaint.color = if (isDark) 0xFF3C4043.toInt() else 0xFFE8EAED.toInt()
        highlightPaint.color = 0x803D7EFF.toInt()
        linePaint.color = if (isDark) 0x33FFFFFF else 0x22000000
        linePaint.strokeWidth = 1f * density
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        val cw = width / columns.toFloat()
        val ch = height / rows.toFloat()
        canvas.drawRect(0f, ch * (rows - 1), width.toFloat(), height.toFloat(), menuPaint)
        for (i in 1 until columns) canvas.drawLine(cw * i, 0f, cw * i, height.toFloat(), linePaint)
        for (i in 1 until rows) canvas.drawLine(0f, ch * i, width.toFloat(), ch * i, linePaint)
        val textColor = if (isDark) 0xFFE8EAED.toInt() else 0xFF3C4043.toInt()
        for (cell in 0 until rows * columns) {
            val r = cell / columns
            val c = cell % columns
            cellRect.set(c * cw, r * ch, (c + 1) * cw, (r + 1) * ch)
            if (cell == highlightCell) {
                val inset = 3f * density
                val hl = RectF(cellRect).apply { inset(inset, inset) }
                canvas.drawRoundRect(hl, 10f * density, 10f * density, highlightPaint)
            }
            val label = when (cell) {
                cancelCell -> "×"
                emojiPageCell -> "☺"
                else -> itemAtCell(cell) ?: continue
            }
            textPaint.textSize = minOf(cw, ch) * if (cell >= emojiCellCount) 0.45f else 0.5f
            textPaint.color = textColor
            val y = cellRect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(label, cellRect.centerX(), y, textPaint)
            if (cell == emojiPageCell) {
                textPaint.textSize = minOf(cw, ch) * 0.16f
                canvas.drawText("一覧", cellRect.centerX(), cellRect.bottom - 6f * density, textPaint)
            }
        }
    }
}
