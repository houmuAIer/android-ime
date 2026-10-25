package com.kazumaproject.markdownhelperkeyboard.atok

import com.kazumaproject.core.domain.extensions.toHankakuKatakana
import com.kazumaproject.hiraToKata
import com.kazumaproject.markdownhelperkeyboard.ime_service.extensions.replaceJapaneseCharactersForEnglish

/** [atok-style] スミレ本体の変換関数をつないで、カナ英数の候補を作る */
object AtokEisuCandidates {

    private fun english(input: String): String? =
        AtokTenkeyAlphabet.toEnglish(input) ?: input.replaceJapaneseCharactersForEnglish()

    /** ポップアップ用（半角, 全角） */
    fun popup(input: String): Pair<List<String>, List<String>> =
        AtokEisuKanaCore.popupCandidates(
            input = input,
            english = { english(it) },
            katakana = { it.hiraToKata() },
            halfKatakana = { it.toHankakuKatakana() },
        )

    /** カナ英数で作られる候補すべて（学習から除外する判定用） */
    fun all(input: String): Set<String> {
        val (half, full) = popup(input)
        return (half + full).toSet()
    }
}
