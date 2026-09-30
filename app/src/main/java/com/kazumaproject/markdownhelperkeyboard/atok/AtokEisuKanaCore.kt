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
        row("やゆよゃゅょ", '8')
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
            val times = timeCandidates(digits)
            times.forEach { result.add(it) }
            times.forEach { result.add(it.toFullWidthAscii()) }
        }
        result.remove(input)
        return result.toList()
    }
}
