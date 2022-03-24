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
    private val reverseMapping: Map<PieceKind, Char> = mapOf(
        PieceKind.LANCE to 'l',
        PieceKind.KNIGHT to 'n',
        PieceKind.SILVER to 's',
        PieceKind.GOLD to 'g',
        PieceKind.KING to 'k',
        PieceKind.PAWN to 'p',
        PieceKind.BISHOP to 'b',
        PieceKind.ROOK to 'r',

        )

    fun covertTo(boardStateFlatten: BoardStateFlatten): String {
        val sb = StringBuilder()
        var emptyCount = 0
        boardStateFlatten.forEachIndexed { index, ps ->
            when (ps.pieceKind) {
                PieceKind.EMPTY -> {
                    emptyCount++
                }
                else -> {
                    if (emptyCount != 0) {
                        sb.append(emptyCount.toString())
                        emptyCount = 0
                    }
                    sb.append(
                        if (ps.isEnemy) reverseMapping[ps.pieceKind] else reverseMapping[ps.pieceKind]!!.uppercase()
                    )

                }
            }
            if (index % BOARD_SIZE == BOARD_SIZE - 1) {

                if (emptyCount != 0) {
                    sb.append(emptyCount.toString())
                    emptyCount = 0
                }
                if (index != BOARD_SIZE * BOARD_SIZE - 1) {
                    sb.append("/")
                }
            }
        }
        return sb.toString()
    }

    fun convertKomadaiTo(pieceCount: Map<PieceKind, Int>, isOwnedEnemy: Boolean): String {
        val sb = StringBuilder()
        listOf(
            PieceKind.PAWN,
            PieceKind.LANCE,
            PieceKind.KNIGHT,
            PieceKind.SILVER,
            PieceKind.BISHOP,
            PieceKind.ROOK,
            PieceKind.GOLD,
        ).filter { pieceCount[it] != null }
            .filter { pieceCount[it]!! >= 1 }
            .forEach {
                if (pieceCount[it]!! > 1) {
                    sb.append(pieceCount[it])
                }
                if (isOwnedEnemy) {
                    sb.append(reverseMapping[it])
                } else {
                    sb.append(reverseMapping[it]!!.uppercase())
                }
            }
        return sb.toString()
    }


    fun convertFrom(sfen: String): BoardStateFlatten {
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