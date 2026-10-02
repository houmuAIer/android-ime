package com.kazumaproject.markdownhelperkeyboard.atok

import com.kazumaproject.core.domain.key.Key
import com.kazumaproject.core.domain.key.KeyInfo
import com.kazumaproject.core.domain.key.KeyMap

/**
 * [atok-style] テンキーのかな入力を、同じキー・同じ方向の英字に読み替える。
 * 対応表はキーボード自体のキー定義（かな／英字）から作るので、
 * 「や」=T、「や＋左フリック（（）」=U、「ゆ（上）」=V のように実際の操作と一致する。
 */
object AtokTenkeyAlphabet {

    private val tenkeyKeys = listOf(
        Key.KeyA, Key.KeyKA, Key.KeySA, Key.KeyTA, Key.KeyNA,
        Key.KeyHA, Key.KeyMA, Key.KeyYA, Key.KeyRA, Key.KeyWA, Key.KeyKutouten,
    )

    /** 濁点・半濁点・小書き・半角かっこを、キー上の元の文字に戻す */
    private val normalize: Map<Char, Char> = buildMap {
        fun pair(from: String, to: String) = from.forEachIndexed { i, c -> put(c, to[i]) }
        pair("がぎぐげご", "かきくけこ")
        pair("ざじずぜぞ", "さしすせそ")
        pair("だぢづでど", "たちつてと")
        pair("ばびぶべぼ", "はひふへほ")
        pair("ぱぴぷぺぽ", "はひふへほ")
        pair("ぁぃぅぇぉ", "あいうえお")
        pair("ゃゅょ", "やゆよ")
        pair("っゎゔ", "つわう")
        pair("()?!", "（）？！")
    }

    private val kanaToEnglish: Map<Char, Char> by lazy {
        val keyMap = KeyMap()
        buildMap {
            for (key in tenkeyKeys) {
                val ja = keyMap.getKeyInfoJapanese(key, false) as? KeyInfo.KeyTapFlickInfo ?: continue
                val en = keyMap.getKeyInfoEnglish(key, false) as? KeyInfo.KeyTapFlickInfo ?: continue
                listOf(
                    ja.tap to en.tap,
                    ja.flickLeft to en.flickLeft,
                    ja.flickTop to en.flickTop,
                    ja.flickRight to en.flickRight,
                    ja.flickBottom to en.flickBottom,
                ).forEach { (kana, letter) ->
                    if (kana != null && letter != null && kana !in this) put(kana, letter)
                }
            }
        }
    }

    /** かな→英字。対応しない文字を含む場合は null */
    fun toEnglish(input: String): String? {
        if (input.isEmpty()) return null
        val sb = StringBuilder(input.length)
        for (c in input) {
            val base = normalize[c] ?: c
            sb.append(kanaToEnglish[base] ?: return null)
        }
        return sb.toString()
    }
}
