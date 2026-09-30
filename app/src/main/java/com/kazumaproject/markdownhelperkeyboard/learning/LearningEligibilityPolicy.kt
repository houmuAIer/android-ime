package com.kazumaproject.markdownhelperkeyboard.learning

import com.kazumaproject.markdownhelperkeyboard.ime_service.extensions.containsSymbolNumberOrEmoji

object LearningEligibilityPolicy {
    fun isEligible(
        input: String,
        output: String,
        allowJapaneseWithSymbolsAndNumbers: Boolean,
    ): Boolean {
        if (input.isBlank() || output.isBlank()) return false
        // [atok-style] ATOKと同じく、記号だけ・数字だけの変換（てん→・ など）も学習する
        if (com.kazumaproject.core.atok.AtokStyle.ENABLED) return true
        if (!output.containsSymbolNumberOrEmoji()) return true
        if (!allowJapaneseWithSymbolsAndNumbers) return false

        // Keep useful mixed phrases such as「令和8年」or「C++入門」while excluding entries that
        // consist only of numbers, symbols, or emoji.
        return output.any { character -> Character.isLetter(character.code) }
    }
}
