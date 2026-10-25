package com.kazumaproject.markdownhelperkeyboard.atok

/**
 * [atok-style] ATOK風「カナ英数」変換の候補文字列を作る（Android 非依存の純粋ロジック）。
 *
 * テンキーのかな入力を「どのキーを押したか」に読み替えて、数字・英字・時刻を作る。
 * 例: 「かさた」→ 234 / ADG / adg / 2:34 / 23時4分 / 2時34分
 */
object AtokEisuKanaCore {

    private val digitByKana: Map<Char, Char> = buildMap {
        fun row(chars: String, digit: Char) = chars.forEach { put(it, digit) }
        row("あいうえおぁぃぅぇぉゔ", '1')
        row("かきくけこがぎぐげご", '2')
        row("さしすせそざじずぜぞ", '3')
        row("たちつてとだぢづでどっ", '4')
        row("なにぬねの", '5')
        row("はひふへほばびぶべぼぱぴぷぺぽ", '6')
        row("まみむめも", '7')
        row("やゆよゃゅょ（）()", '8')
        row("らりるれろ", '9')
        row("わをんゎー", '0')
    }

    /** かな→テンキーの数字列。対応しない文字を含む場合は null */
    fun toKeyDigits(input: String): String? {
        if (input.isEmpty()) return null
        val sb = StringBuilder(input.length)
        for (c in input) {
            sb.append(digitByKana[c] ?: return null)
        }
        return sb.toString()
    }

    /**
     * 3〜4桁の数字列から時刻表記を作る。
     * 「:」表記は分が2桁のときだけ、「時分」表記は時0〜23・分0〜59の分け方をすべて出す。
     */
    fun timeCandidates(digits: String): List<String> {
        if (digits.length !in 3..4 || !digits.all { it in '0'..'9' }) return emptyList()
        val colon = mutableListOf<String>()
        val jikan = mutableListOf<String>()
        for (split in digits.length - 1 downTo 1) {
            val h = digits.substring(0, split)
            val m = digits.substring(split)
            if (h.length > 2 || m.length > 2) continue
            val hour = h.toInt()
            val minute = m.toInt()
            if (hour > 23 || minute > 59) continue
            if (m.length == 2) colon.add("$h:$m")
            jikan.add("${hour}時${minute}分")
        }
        return colon + jikan
    }

    private val daysInMonth = intArrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    /**
     * 3〜4桁の数字列から日付表記を作る（1214 → 12月14日 / 12/14、121 → 1月21日・12月1日）。
     * 月1〜12・その月にある日（2月は29日まで）の分け方をすべて出す。
     */
    fun dateCandidates(digits: String): List<String> {
        if (digits.length !in 3..4 || !digits.all { it in '0'..'9' }) return emptyList()
        val kanji = mutableListOf<String>()
        val slash = mutableListOf<String>()
        for (split in 1 until digits.length) {
            val m = digits.substring(0, split)
            val d = digits.substring(split)
            if (m.length > 2 || d.length > 2) continue
            if (m.startsWith("0") || d.startsWith("0")) continue
            val month = m.toInt()
            val day = d.toInt()
            if (month !in 1..12 || day !in 1..daysInMonth[month - 1]) continue
            kanji.add("${month}月${day}日")
            slash.add("$month/$day")
        }
        return kanji + slash
    }

    private fun String.toFullWidthAscii(): String =
        map { c -> if (c in '!'..'~') (c.code + 0xFEE0).toChar() else c }.joinToString("")

    /**
     * 候補文字列（表示順）。
     * @param english かな→英字の読み替え（本家スミレの対応表を渡す）
     * @param katakana かな→全角カタカナ
     * @param halfKatakana かな→半角カタカナ
     */
    fun candidateStrings(
        input: String,
        english: (String) -> String,
        katakana: (String) -> String,
        halfKatakana: (String) -> String,
    ): List<String> {
        if (input.isEmpty()) return emptyList()
        val result = LinkedHashSet<String>()
        result.add(katakana(input))
        result.add(halfKatakana(input))
        val digits = toKeyDigits(input)
        if (digits != null) {
            result.add(digits)
            result.add(digits.toFullWidthAscii())
        }
        val en = english(input)
        if (en.isNotEmpty() && en.all { it in ' '..'~' }) {
            result.add(en.uppercase())
            result.add(en.lowercase())
            result.add(en.uppercase().toFullWidthAscii())
            result.add(en.lowercase().toFullWidthAscii())
        }
        if (digits != null) {
            val times = timeCandidates(digits) + dateCandidates(digits)
            times.forEach { result.add(it) }
            times.forEach { result.add(it.toFullWidthAscii()) }
        }
        result.remove(input)
        return result.toList()
    }

    /**
     * ATOK風のカナ英数ポップアップ用に、半角と全角の候補を分けて返す。
     * 半角: ｶﾅ / 数字 / 英字(大・小・先頭大) / 時刻 / 日付
     * 全角: カナ / 数字 / 英字(大・小・先頭大) / 時刻 / 日付
     */
    fun popupCandidates(
        input: String,
        english: (String) -> String?,
        katakana: (String) -> String,
        halfKatakana: (String) -> String,
    ): Pair<List<String>, List<String>> {
        if (input.isEmpty()) return emptyList<String>() to emptyList()
        val half = LinkedHashSet<String>()
        half.add(halfKatakana(input))
        val digits = toKeyDigits(input)
        if (digits != null) half.add(digits)
        val en = english(input)?.takeIf { it.isNotEmpty() && it.all { c -> c in ' '..'~' } }
        if (en != null) {
            half.add(en.uppercase())
            half.add(en.lowercase())
            half.add(en.lowercase().replaceFirstChar { it.uppercaseChar() })
        }
        if (digits != null) {
            timeCandidates(digits).forEach { half.add(it) }
            dateCandidates(digits).forEach { half.add(it) }
        }
        val full = LinkedHashSet<String>()
        full.add(katakana(input))
        half.drop(1).forEach { full.add(it.toFullWidthAscii()) }
        half.remove(input)
        full.remove(input)
        return half.toList() to full.toList()
    }
}
