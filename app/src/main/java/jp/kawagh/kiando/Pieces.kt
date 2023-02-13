package jp.kawagh.kiando

typealias BoardState = List<List<PanelState>>
typealias BoardStateFlatten = List<PanelState>

enum class PieceKind {
    KING,
    ROOK,
    BISHOP,
    GOLD,
    SILVER,
    KNIGHT,
    LANCE,
    PAWN,
    EMPTY,
}

data class Position(val row: Int, val column: Int)

data class Move(
    val from: Position,
    val to: Position,
    val isPromote: Boolean = false
)

fun Move.toReadable(pieceKind: PieceKind): String {
    val pieceText = when (pieceKind) {
        PieceKind.EMPTY -> ""
        PieceKind.KING -> "王"
        PieceKind.ROOK -> "飛"
        PieceKind.BISHOP -> "角"
        PieceKind.GOLD -> "金"
        PieceKind.SILVER -> "銀"
        PieceKind.KNIGHT -> "桂"
        PieceKind.LANCE -> "香"
        PieceKind.PAWN -> "歩"
    }
    val kanji = "一二三四五六七八九"
    return "${9 - this.to.column}" + "${kanji[this.to.row]}" + pieceText + if (this.isPromote) {
        "成"
    } else ""
}

val NonPosition = Position(-1, -1)
val NonMove = Move(NonPosition, NonPosition)

data class PanelState(
    val row: Int,
    val column: Int,
    val pieceKind: PieceKind,
    val isEnemy: Boolean = false,
    val isPromoted: Boolean = false,
)


val initialBoardState: BoardState = listOf(
    listOf(
        PanelState(
            0, 0, PieceKind.LANCE, isEnemy = true
        ),
        PanelState(0, 1, PieceKind.KNIGHT, isEnemy = true),
        PanelState(0, 2, PieceKind.SILVER, isEnemy = true),
        PanelState(0, 3, PieceKind.GOLD, isEnemy = true),
        PanelState(0, 4, PieceKind.KING, isEnemy = true),
        PanelState(0, 5, PieceKind.GOLD, isEnemy = true),
        PanelState(0, 6, PieceKind.SILVER, isEnemy = true),
        PanelState(0, 7, PieceKind.KNIGHT, isEnemy = true),
        PanelState(0, 8, PieceKind.LANCE, isEnemy = true),
    ),
    listOf(
        PanelState(1, 0, PieceKind.EMPTY),
        PanelState(1, 1, PieceKind.ROOK, true),
        PanelState(1, 2, PieceKind.EMPTY),
        PanelState(1, 3, PieceKind.EMPTY),
        PanelState(1, 4, PieceKind.EMPTY),
        PanelState(1, 5, PieceKind.EMPTY),
        PanelState(1, 6, PieceKind.EMPTY),
        PanelState(1, 7, PieceKind.BISHOP, true),
        PanelState(1, 8, PieceKind.EMPTY),
    ),

    listOf(
        PanelState(2, 0, PieceKind.PAWN, isEnemy = true),
        PanelState(2, 1, PieceKind.PAWN, isEnemy = true),
        PanelState(2, 2, PieceKind.PAWN, isEnemy = true),
        PanelState(2, 3, PieceKind.PAWN, isEnemy = true),
        PanelState(2, 4, PieceKind.PAWN, isEnemy = true),
        PanelState(2, 5, PieceKind.PAWN, isEnemy = true),
        PanelState(2, 6, PieceKind.PAWN, isEnemy = true),
        PanelState(2, 7, PieceKind.PAWN, isEnemy = true),
        PanelState(2, 8, PieceKind.PAWN, isEnemy = true),
    ),

    listOf(
        PanelState(3, 0, PieceKind.EMPTY),
        PanelState(3, 1, PieceKind.EMPTY),
        PanelState(3, 2, PieceKind.EMPTY),
        PanelState(3, 3, PieceKind.EMPTY),
        PanelState(3, 4, PieceKind.EMPTY),
        PanelState(3, 5, PieceKind.EMPTY),
        PanelState(3, 6, PieceKind.EMPTY),
        PanelState(3, 7, PieceKind.EMPTY),
        PanelState(3, 8, PieceKind.EMPTY)
    ),

    listOf(
        PanelState(4, 0, PieceKind.EMPTY),
        PanelState(4, 1, PieceKind.EMPTY),
        PanelState(4, 2, PieceKind.EMPTY),
        PanelState(4, 3, PieceKind.EMPTY),
        PanelState(4, 4, PieceKind.EMPTY),
        PanelState(4, 5, PieceKind.EMPTY),
        PanelState(4, 6, PieceKind.EMPTY),
        PanelState(4, 7, PieceKind.EMPTY),
        PanelState(4, 8, PieceKind.EMPTY)
    ),


    listOf(
        PanelState(5, 0, PieceKind.EMPTY),
        PanelState(5, 1, PieceKind.EMPTY),
        PanelState(5, 2, PieceKind.EMPTY),
        PanelState(5, 3, PieceKind.EMPTY),
        PanelState(5, 4, PieceKind.EMPTY),
        PanelState(5, 5, PieceKind.EMPTY),
        PanelState(5, 6, PieceKind.EMPTY),
        PanelState(5, 7, PieceKind.EMPTY),
        PanelState(5, 8, PieceKind.EMPTY)
    ),

    listOf(
        PanelState(6, 0, PieceKind.PAWN, isEnemy = false),
        PanelState(6, 1, PieceKind.PAWN, isEnemy = false),
        PanelState(6, 2, PieceKind.PAWN, isEnemy = false),
        PanelState(6, 3, PieceKind.PAWN, isEnemy = false),
        PanelState(6, 4, PieceKind.PAWN, isEnemy = false),
        PanelState(6, 5, PieceKind.PAWN, isEnemy = false),
        PanelState(6, 6, PieceKind.PAWN, isEnemy = false),
        PanelState(6, 7, PieceKind.PAWN, isEnemy = false),
        PanelState(6, 8, PieceKind.PAWN, isEnemy = false),
    ),

    listOf(
        PanelState(
            7, 0, PieceKind.EMPTY
        ),
        PanelState(7, 1, PieceKind.BISHOP, false),
        PanelState(7, 2, PieceKind.EMPTY),
        PanelState(7, 3, PieceKind.EMPTY),
        PanelState(7, 4, PieceKind.EMPTY),
        PanelState(7, 5, PieceKind.EMPTY),
        PanelState(7, 6, PieceKind.EMPTY),
        PanelState(7, 7, PieceKind.ROOK, false),
        PanelState(7, 8, PieceKind.EMPTY),
    ),

    listOf(
        PanelState(8, 0, PieceKind.LANCE, isEnemy = false),
        PanelState(8, 1, PieceKind.KNIGHT, isEnemy = false),
        PanelState(8, 2, PieceKind.SILVER, isEnemy = false),
        PanelState(8, 3, PieceKind.GOLD, isEnemy = false),
        PanelState(8, 4, PieceKind.KING, isEnemy = false),
        PanelState(8, 5, PieceKind.GOLD, isEnemy = false),
        PanelState(8, 6, PieceKind.SILVER, isEnemy = false),
        PanelState(8, 7, PieceKind.KNIGHT, isEnemy = false),
        PanelState(8, 8, PieceKind.LANCE, isEnemy = false),
    ),
)