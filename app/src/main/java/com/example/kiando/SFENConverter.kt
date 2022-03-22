package com.example.kiando


class SFENConverter {
    // TODO Promotion
    private val mapping: Map<Char, Pair<PieceKind, Boolean>> = mapOf(
        'l' to Pair(PieceKind.LANCE, true),
        'L' to Pair(PieceKind.LANCE, false),
        'n' to Pair(PieceKind.KNIGHT, true),
        'N' to Pair(PieceKind.KNIGHT, false),
        's' to Pair(PieceKind.SILVER, true),
        'S' to Pair(PieceKind.SILVER, false),
        'g' to Pair(PieceKind.GOLD, true),
        'G' to Pair(PieceKind.GOLD, false),
        'k' to Pair(PieceKind.KING, true),
        'K' to Pair(PieceKind.KING, false),
        'p' to Pair(PieceKind.PAWN, true),
        'P' to Pair(PieceKind.PAWN, false),
        'b' to Pair(PieceKind.BISHOP, true),
        'B' to Pair(PieceKind.BISHOP, false),
        'r' to Pair(PieceKind.ROOK, true),
        'R' to Pair(PieceKind.ROOK, false),
    )

    fun convertFrom(sfen: String): List<PanelState> {
        val board = MutableList(BOARD_SIZE * BOARD_SIZE) {
            PanelState(it / BOARD_SIZE, it % BOARD_SIZE, PieceKind.EMPTY)
        }
        var i = 0
        for (ch in sfen) {
            when (ch) {
                in mapping.keys -> {
                    board[i] = PanelState(
                        i / BOARD_SIZE,
                        i % BOARD_SIZE,
                        pieceKind = mapping[ch]!!.first,
                        isEnemy = mapping[ch]!!.second,
                    )
                    i += 1
                }
                in '1'..'9' -> {
                    val digit: Int = ch - '0'
                    repeat(digit) {
                        val ni = i + it
                        board[ni] = PanelState(
                            ni / BOARD_SIZE,
                            ni % BOARD_SIZE,
                            pieceKind = PieceKind.EMPTY,
                            isEnemy = false,
                        )
                    }
                    i += digit
                }
                ' ' -> break //盤面以降は読み込まない
            }
        }
        return board.toList()
    }
}