package com.kazumaproject.tenkey.view

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

/**
 * [atok-style] 絵文字キーから指を離さずにスライドしたときに、キーボード全体に重ねて出す一覧。
 * 上3段ぶんの領域に絵文字を 8列×5段（最大40個）で並べ、最下段（キー1段ぶん）は操作用で
 * キーと同じ5列。左端（絵文字キーの位置）が「×」＝入力しない、その右が「絵文字一覧を開く」。
 */
class AtokEmojiSwipePanel(context: Context) : View(context) {

    companion object {
        const val EMOJI_COLUMNS = 8
        const val EMOJI_ROWS = 5
        const val MAX_ITEMS = EMOJI_COLUMNS * EMOJI_ROWS
        private const val MENU_COLUMNS = 5
        private const val MENU_BASE = 1000
        private const val CANCEL_CELL = MENU_BASE
        private const val EMOJI_PAGE_CELL = MENU_BASE + 1
        private const val CLIPBOARD_CELL = MENU_BASE + 2
    }

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

    fun isEmojiPageCell(cell: Int): Boolean = cell == EMOJI_PAGE_CELL

    fun isClipboardCell(cell: Int): Boolean = cell == CLIPBOARD_CELL

    private val menuTop: Float get() = height * 3f / 4f

    /** 画面座標からマス番号（絵文字 0〜、操作段 1000〜）。範囲外は -1 */
    fun cellAtRaw(rawX: Float, rawY: Float): Int {
        if (width <= 0 || height <= 0) return -1
        val loc = IntArray(2)
        getLocationOnScreen(loc)
        val x = rawX - loc[0]
        val y = rawY - loc[1]
        if (x < 0 || y < 0 || x >= width || y >= height) return -1
        return if (y >= menuTop) {
            MENU_BASE + (x / (width / MENU_COLUMNS.toFloat())).toInt().coerceIn(0, MENU_COLUMNS - 1)
        } else {
            val c = (x / (width / EMOJI_COLUMNS.toFloat())).toInt().coerceIn(0, EMOJI_COLUMNS - 1)
            val r = (y / (menuTop / EMOJI_ROWS)).toInt().coerceIn(0, EMOJI_ROWS - 1)
            r * EMOJI_COLUMNS + c
        }
    }

    /** マスの絵文字。操作段・空きマス・範囲外は null */
    fun itemAtCell(cell: Int): String? {
        if (cell < 0 || cell >= MENU_BASE) return null
        return items.getOrNull(cell)
    }

    private fun cellRect(cell: Int): RectF {
        return if (cell >= MENU_BASE) {
            val cw = width / MENU_COLUMNS.toFloat()
            val c = cell - MENU_BASE
            RectF(c * cw, menuTop, (c + 1) * cw, height.toFloat())
        } else {
            val cw = width / EMOJI_COLUMNS.toFloat()
            val ch = menuTop / EMOJI_ROWS
            val r = cell / EMOJI_COLUMNS
            val c = cell % EMOJI_COLUMNS
            RectF(c * cw, r * ch, (c + 1) * cw, (r + 1) * ch)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return
        val density = resources.displayMetrics.density
        bgPaint.color = if (isDark) 0xFA202124.toInt() else 0xFAF8F9FA.toInt()
        menuPaint.color = if (isDark) 0xFF3C4043.toInt() else 0xFFE8EAED.toInt()
        highlightPaint.color = 0x803D7EFF.toInt()
        linePaint.color = if (isDark) 0x33FFFFFF else 0x22000000
        linePaint.strokeWidth = 1f * density
        val textColor = if (isDark) 0xFFE8EAED.toInt() else 0xFF3C4043.toInt()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        canvas.drawRect(0f, menuTop, width.toFloat(), height.toFloat(), menuPaint)
        val menuCw = width / MENU_COLUMNS.toFloat()
        for (i in 1 until MENU_COLUMNS) {
            canvas.drawLine(menuCw * i, menuTop, menuCw * i, height.toFloat(), linePaint)
        }

        if (highlightCell >= 0) {
            val hl = cellRect(highlightCell).apply { inset(2f * density, 2f * density) }
            canvas.drawRoundRect(hl, 8f * density, 8f * density, highlightPaint)
        }

        val emojiCw = width / EMOJI_COLUMNS.toFloat()
        val emojiCh = menuTop / EMOJI_ROWS
        textPaint.textSize = minOf(emojiCw, emojiCh) * 0.62f
        for (i in 0 until minOf(items.size, MAX_ITEMS)) {
            val rect = cellRect(i)
            val y = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(items[i], rect.centerX(), y, textPaint)
        }

        textPaint.color = textColor
        val menuH = height - menuTop
        for ((cell, label) in listOf(CANCEL_CELL to "×", EMOJI_PAGE_CELL to "☺", CLIPBOARD_CELL to "📋")) {
            val rect = cellRect(cell)
            textPaint.textSize = minOf(menuCw, menuH) * 0.42f
            val y = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(label, rect.centerX(), y, textPaint)
            if (cell == EMOJI_PAGE_CELL || cell == CLIPBOARD_CELL) {
                textPaint.textSize = minOf(menuCw, menuH) * 0.16f
                canvas.drawText(
                    if (cell == EMOJI_PAGE_CELL) "一覧" else "履歴",
                    rect.centerX(), rect.bottom - 6f * density, textPaint
                )
            }
        }
    }
}
