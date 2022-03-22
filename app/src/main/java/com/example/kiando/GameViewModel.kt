package com.example.kiando

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel

const val BOARD_SIZE = 9

class GameViewModel : ViewModel() {
    var boardState: SnapshotStateList<PanelState> =
        List(9 * 9) {
            PanelState(it / 9, it % 9, PieceKind.EMPTY)
        }.toMutableStateList()

    init {
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                val index = i * 9 + j
                boardState[index] = initialBoardState[i][j]
            }
        }
    }

    fun move(move: Move) {
        val fromIndex = move.from.row * BOARD_SIZE + move.from.column
        val toIndex = move.to.row * BOARD_SIZE + move.to.column
        if (fromIndex == toIndex) return
        val panelState = boardState[fromIndex]
        if (isValidMove(move, panelState)) {
            boardState[toIndex] =
                PanelState(move.to.row, move.to.column, boardState[fromIndex].pieceKind)
            boardState[fromIndex] = PanelState(move.from.row, move.from.column, PieceKind.EMPTY)
        }
    }

    private fun isValidMove(move: Move, panelState: PanelState): Boolean {
        return move.to in listPotentialMoves(panelState)
    }


    fun listPotentialMoves(panelState: PanelState): List<Position> {
        val originalRow = panelState.row
        val originalColumn = panelState.column
        val list = if (panelState.isEnemy) listOf() else
            when (panelState.pieceKind) {
                PieceKind.PAWN -> listOf(Position(originalRow - 1, originalColumn))
                // TODO implement below
                // should know boardState to avoid overlap
                // promotion
                PieceKind.EMPTY -> listOf()
                PieceKind.KING -> (-1..1).map { dx ->
                    (-1..1).map { dy ->
                        Position(originalRow + dx, originalColumn + dy)
                    }
                }.flatten()
                PieceKind.ROOK -> (1 until 9).map { length ->
                    (0 until 4).map { dir ->
                        when (dir) {
                            0 -> Position(originalRow, originalColumn + length)
                            1 -> Position(originalRow - length, originalColumn)
                            2 -> Position(originalRow, originalColumn - length)
                            else -> Position(originalRow + length, originalColumn)
                        }
                    }
                }.flatten()
                PieceKind.BISHOP -> (1 until 9).map { length ->
                    (0 until 4).map { dir ->
                        when (dir) {
                            0 -> Position(originalRow - length, originalColumn + length)
                            1 -> Position(originalRow - length, originalColumn - length)
                            2 -> Position(originalRow + length, originalColumn - length)
                            else -> Position(originalRow + length, originalColumn + length)
                        }
                    }
                }.flatten()
                PieceKind.GOLD -> (-1..1).map { dx ->
                    (-1..1).map { dy ->
                        Position(originalRow + dx, originalColumn + dy)
                    }
                }.flatten().filterNot {
                    it == Position(originalRow + 1, originalColumn + 1)
                            || it == Position(originalRow + 1, originalColumn - 1)
                }
                PieceKind.SILVER -> listOf(
                    Position(originalRow - 1, originalColumn - 1),
                    Position(originalRow - 1, originalColumn),
                    Position(originalRow - 1, originalColumn + 1),
                    Position(originalRow + 1, originalColumn + 1),
                    Position(originalRow + 1, originalColumn - 1),
                )
                PieceKind.KNIGHT -> listOf(
                    Position(originalRow - 2, originalColumn + 1),
                    Position(originalRow - 2, originalColumn - 1),
                )
                PieceKind.LANCE -> (0 until 9).map { Position(originalRow - it, originalColumn) }
            }
        return list
    }

}