package com.example.kiando

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

sealed class PanelState {
    data class Piece(
        val pieceKind: PieceKind,
        val isEnemy: Boolean,
        val isPromoted: Boolean = false
    ) : PanelState()

    object Empty : PanelState()
}


val initialBoardState: List<List<PanelState>> = listOf(
    listOf(
        PanelState.Piece(PieceKind.LANCE, isEnemy = true),
        PanelState.Piece(PieceKind.KNIGHT, isEnemy = true),
        PanelState.Piece(PieceKind.SILVER, isEnemy = true),
        PanelState.Piece(PieceKind.GOLD, isEnemy = true),
        PanelState.Piece(PieceKind.KING, isEnemy = true),
        PanelState.Piece(PieceKind.GOLD, isEnemy = true),
        PanelState.Piece(PieceKind.SILVER, isEnemy = true),
        PanelState.Piece(PieceKind.KNIGHT, isEnemy = true),
        PanelState.Piece(PieceKind.LANCE, isEnemy = true),
    ),
    listOf(
        PanelState.Empty,
        PanelState.Piece(PieceKind.ROOK, true),
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Piece(PieceKind.BISHOP, true),
        PanelState.Empty,
    ),

    listOf(
        PanelState.Piece(PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(PieceKind.PAWN, isEnemy = true),
        PanelState.Piece(PieceKind.PAWN, isEnemy = true),
    ),
    listOf(
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
    ),
    listOf(
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
    ),
    listOf(
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
    ),
    listOf(
        PanelState.Piece(PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(PieceKind.PAWN, isEnemy = false),
        PanelState.Piece(PieceKind.PAWN, isEnemy = false),
    ),
    listOf(
        PanelState.Empty,
        PanelState.Piece(PieceKind.BISHOP, false),
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Empty,
        PanelState.Piece(PieceKind.ROOK, false),
        PanelState.Empty,
    ),
    listOf(
        PanelState.Piece(PieceKind.LANCE, isEnemy = false),
        PanelState.Piece(PieceKind.KNIGHT, isEnemy = false),
        PanelState.Piece(PieceKind.SILVER, isEnemy = false),
        PanelState.Piece(PieceKind.GOLD, isEnemy = false),
        PanelState.Piece(PieceKind.KING, isEnemy = false),
        PanelState.Piece(PieceKind.GOLD, isEnemy = false),
        PanelState.Piece(PieceKind.SILVER, isEnemy = false),
        PanelState.Piece(PieceKind.KNIGHT, isEnemy = false),
        PanelState.Piece(PieceKind.LANCE, isEnemy = false),
    ),
)