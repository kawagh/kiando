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

    private fun posToIndex(position: Position): Int = position.row * BOARD_SIZE + position.column

    private fun isInside(position: Position): Boolean =
        0 <= posToIndex(position) && posToIndex(position) < BOARD_SIZE * BOARD_SIZE

    private fun isValidMove(move: Move, panelState: PanelState): Boolean {
        return move.to in listLegalMoves(panelState)
    }

    fun listLegalMoves(panelState: PanelState): List<Position> {
        val originalRow = panelState.row
        val originalColumn = panelState.column
        val resluts = if (panelState.isEnemy) listOf() else
            when (panelState.pieceKind) {
                // TODO promotion
                PieceKind.EMPTY -> {
                    return listOf()
                }
                PieceKind.PAWN -> listOf(Position(originalRow - 1, originalColumn))
                    .filter {
                        isInside(it) && (boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY || boardState[posToIndex(
                            it
                        )].isEnemy)
                    }
                PieceKind.KING -> (-1..1).map { dx ->
                    (-1..1).map { dy ->
                        Position(originalRow + dx, originalColumn + dy)
                    }
                }.flatten()
                    .filter {
                        isInside(it) && (boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY || boardState[posToIndex(
                            it
                        )].isEnemy)
                    }
                // 線駒は自駒に衝突するかはじめに遭遇する敵駒マスまで進める
                PieceKind.ROOK -> {
                    val nextPositions = mutableListOf<Position>()
                    for (dir in 0 until 4) {
                        for (length in 1 until 9) {
                            val nextPosition = when (dir) {
                                0 -> Position(originalRow, originalColumn + length)
                                1 -> Position(originalRow - length, originalColumn)
                                2 -> Position(originalRow, originalColumn - length)
                                else -> Position(originalRow + length, originalColumn)
                            }
                            if (!isInside(nextPosition)) break
                            if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY
                                || boardState[posToIndex(nextPosition)].isEnemy
                            ) {
                                nextPositions.add(nextPosition)
                            }
                            if (boardState[posToIndex(nextPosition)].pieceKind != PieceKind.EMPTY) break
                        }
                    }
                    return nextPositions.toList()
                }
                PieceKind.BISHOP -> {
                    val nextPositions = mutableListOf<Position>()
                    for (dir in 0 until 4) {
                        for (length in 1 until 9) {
                            val nextPosition = when (dir) {
                                0 -> Position(originalRow - length, originalColumn + length)
                                1 -> Position(originalRow - length, originalColumn - length)
                                2 -> Position(originalRow + length, originalColumn - length)
                                else -> Position(originalRow + length, originalColumn + length)
                            }
                            if (!isInside(nextPosition)) break

                            if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY
                                || boardState[posToIndex(nextPosition)].isEnemy
                            ) {
                                nextPositions.add(nextPosition)
                            }
                            if (boardState[posToIndex(nextPosition)].pieceKind != PieceKind.EMPTY) break
                        }
                    }
                    return nextPositions.toList()
                }
                PieceKind.GOLD -> (-1..1).map { dx ->
                    (-1..1).map { dy ->
                        Position(originalRow + dx, originalColumn + dy)
                    }
                }.flatten().filterNot {
                    it == Position(originalRow + 1, originalColumn + 1)
                            || it == Position(originalRow + 1, originalColumn - 1)
                }.filter {
                    isInside(it) && (
                            boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY
                                    || boardState[posToIndex(it)].isEnemy)
                }
                PieceKind.SILVER -> listOf(
                    Position(originalRow - 1, originalColumn - 1),
                    Position(originalRow - 1, originalColumn),
                    Position(originalRow - 1, originalColumn + 1),
                    Position(originalRow + 1, originalColumn + 1),
                    Position(originalRow + 1, originalColumn - 1),
                )
                    .filter {
                        isInside(it) && (boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY || boardState[posToIndex(
                            it
                        )].isEnemy)
                    }
                PieceKind.KNIGHT -> listOf(
                    Position(originalRow - 2, originalColumn + 1),
                    Position(originalRow - 2, originalColumn - 1),
                ).filter {
                    isInside(it) && (boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY || boardState[posToIndex(
                        it
                    )].isEnemy)
                }
                PieceKind.LANCE -> {
                    val nextPositions = mutableListOf<Position>()
                    for (length in 1 until 9) {
                        val nextPosition = Position(originalRow - length, originalColumn)
                        if (!isInside(nextPosition)) break
                        if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY
                            || boardState[posToIndex(nextPosition)].isEnemy
                        ) {
                            nextPositions.add(nextPosition)
                        }
                        if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY) break
                    }
                    return nextPositions.toList()
                }
            }
        return resluts
    }
}