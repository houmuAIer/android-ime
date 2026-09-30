package com.kazumaproject.core.atok

/**
 * [atok-style] 削除キーを指を離さずにスライドしたときの動作。
 * 左へ1段=文(単語)削除、2段=左全削除。上へ1段=Delete、2段=右全削除。
 */
enum class AtokDeleteSlideAction(val label: String) {
    WORD_BEFORE("文(単語)\n削除"),
    ALL_BEFORE("左全削除"),
    FORWARD_ONE("Delete"),
    ALL_AFTER("右全削除"),
}
