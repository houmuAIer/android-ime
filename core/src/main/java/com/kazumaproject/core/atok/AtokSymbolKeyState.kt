package com.kazumaproject.core.atok

/** [atok-style] 左下キー（絵文字・記号キー）の表示状態 */
enum class AtokSymbolKeyState {
    /** 未入力: 絵文字・記号 */
    DEFAULT,

    /** 入力中: カナ英数 */
    KANA_EISU,

    /** 確定直後: 後変換（再変換） */
    RECONVERT,
}
