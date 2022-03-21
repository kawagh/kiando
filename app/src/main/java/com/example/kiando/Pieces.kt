package com.example.kiando

typealias BoardState = List<List<PanelState>>

enum class PieceKind {
    KING,
    ROOK,
    BISHOP,
    GOLD,
    SILVER,
    KNIGHT,
    LANCE,
    PAWN,
}

data class Position(val row: Int, val column: Int)

fun listLegalMoves(panelState: PanelState): List<Position> {
    val originalRow = panelState.row
    val originalColumn = panelState.column
    val todoLegalMoves = listOf(Position(4, 4))
    val list = when (panelState) {
        is PanelState.Empty -> listOf<Position>()
        is PanelState.Piece -> when (panelState.pieceKind) {
            PieceKind.PAWN -> listOf(Position(originalRow - 1, originalColumn))
            // TODO implement below
            //    should know boardState
            PieceKind.KING -> listOf((-1..1).forEach { dx ->
                (-1..1).forEach { dy ->
                    Position(originalRow + dx, originalColumn + dy)
                }
            }) as List<Position> // FIXME not work
            PieceKind.ROOK -> todoLegalMoves
            PieceKind.BISHOP -> todoLegalMoves
            PieceKind.GOLD -> todoLegalMoves
            PieceKind.SILVER -> todoLegalMoves
            PieceKind.KNIGHT -> listOf(
                Position(originalRow - 2, originalColumn + 1),
                Position(originalRow - 2, originalColumn - 1),
            )
            PieceKind.LANCE -> todoLegalMoves
        }
    }
    return list
}

sealed class PanelState {
    abstract val row: Int
    abstract val column: Int

    data class Piece(
        override val row: Int,
        override val column: Int,
        val pieceKind: PieceKind,
        val isEnemy: Boolean,
        val isPromoted: Boolean = false
    ) : PanelState()

    data class Empty(override val row: Int, override val column: Int) : PanelState()
}


val initialBoardState: BoardState = listOf(
    listOf(
        PanelState.Piece(0, 0, PieceKind.LANCE, isEnemy = true),
        PanelState.Piece(0, 1, PieceKind.KNIGHT, isEnemy = true),
        PanelState.Piece(0, 2, PieceKind.SILVER, isEnemy = true),
        PanelState.Piece(0, 3, PieceKind.GOLD, isEnemy = true),
        PanelState.Piece(0, 4, PieceKind.KING, isEnemy = true),
        PanelState.Piece(0, 5, PieceKind.GOLD, isEnemy = true),
        PanelState.Piece(0, 6, PieceKind.SILVER, isEnemy = true),
        PanelState.Piece(0, 7, PieceKind.KNIGHT, isEnemy = true),
        PanelState.Piece(0, 8, PieceKind.LANCE, isEnemy = true),
    ),
    listOf(
        PanelState.Empty(1, 0),
        PanelState.Piece(1, 1, PieceKind.ROOK, true),
        PanelState.Empty(1, 2),
        PanelState.Empty(1, 3),
        PanelState.Empty(1, 4),
        PanelState.Empty(1, 5),
        PanelState.Empty(1, 6),
        PanelState.Piece(1, 7, PieceKind.BISHOP, true),
        PanelState.Empty(1, 8)
    ),

    listOf(
        PanelState.Piece(2, 0, PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(2, 1, PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(2, 2, PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(2, 3, PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(2, 4, PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(2, 5, PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(2, 6, PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(2, 7, PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(2, 8, PieceKind.PAWN, isEnemy = true),
    ),

    listOf(
        PanelState.Empty(3, 0),
        PanelState.Empty(3, 1),
        PanelState.Empty(3, 2),
        PanelState.Empty(3, 3),
        PanelState.Empty(3, 4),
        PanelState.Empty(3, 5),
        PanelState.Empty(3, 6),
        PanelState.Empty(3, 7),
        PanelState.Empty(3, 8)
    ),
    listOf(
        PanelState.Empty(4, 0),
        PanelState.Empty(4, 1),
        PanelState.Empty(4, 2),
        PanelState.Empty(4, 3),
        PanelState.Empty(4, 4),
        PanelState.Empty(4, 5),
        PanelState.Empty(4, 6),
        PanelState.Empty(4, 7),
        PanelState.Empty(4, 8)
    ),
    listOf(
        PanelState.Empty(5, 0),
        PanelState.Empty(5, 1),
        PanelState.Empty(5, 2),
        PanelState.Empty(5, 3),
        PanelState.Empty(5, 4),
        PanelState.Empty(5, 5),
        PanelState.Empty(5, 6),
        PanelState.Empty(5, 7),
        PanelState.Empty(5, 8)
    ),

    listOf(
        PanelState.Piece(6, 0, PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(6, 1, PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(6, 2, PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(6, 3, PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(6, 4, PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(6, 5, PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(6, 6, PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(6, 7, PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(6, 8, PieceKind.PAWN, isEnemy = false),
    ),

    listOf(
        PanelState.Empty(7, 0),
        PanelState.Piece(7, 1, PieceKind.BISHOP, false),
        PanelState.Empty(7, 2),
        PanelState.Empty(7, 3),
        PanelState.Empty(7, 4),
        PanelState.Empty(7, 5),
        PanelState.Empty(7, 6),
        PanelState.Piece(7, 7, PieceKind.ROOK, false),
        PanelState.Empty(7, 8)
    ),

    listOf(
        PanelState.Piece(8, 0, PieceKind.LANCE, isEnemy = false),
        PanelState.Piece(8, 1, PieceKind.KNIGHT, isEnemy = false),
        PanelState.Piece(8, 2, PieceKind.SILVER, isEnemy = false),
        PanelState.Piece(8, 3, PieceKind.GOLD, isEnemy = false),
        PanelState.Piece(8, 4, PieceKind.KING, isEnemy = false),
        PanelState.Piece(8, 5, PieceKind.GOLD, isEnemy = false),
        PanelState.Piece(8, 6, PieceKind.SILVER, isEnemy = false),
        PanelState.Piece(8, 7, PieceKind.KNIGHT, isEnemy = false),
        PanelState.Piece(8, 8, PieceKind.LANCE, isEnemy = false),
    ),
)