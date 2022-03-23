package com.example.kiando

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel

const val BOARD_SIZE = 9

class GameViewModel : ViewModel() {
    var boardState: SnapshotStateList<PanelState> = initialBoardState.flatten().toMutableStateList()


    fun loadQuestion(questionId: Int) {
        boardState = sampleQuestions[questionId].boardState.toMutableStateList()

    }

    fun isPromotable(move: Move): Boolean {
        val fromIndex = move.from.row * BOARD_SIZE + move.from.column
        return !boardState[fromIndex].isPromoted && (move.from.row <= 2 || move.to.row <= 2)
    }

    fun move(move: Move) {
        val fromIndex = move.from.row * BOARD_SIZE + move.from.column
        val toIndex = move.to.row * BOARD_SIZE + move.to.column
        if (fromIndex == toIndex) return
        val panelState = boardState[fromIndex]
        if (isValidMove(move, panelState)) {
            boardState[toIndex] =
                PanelState(
                    move.to.row,
                    move.to.column,
                    boardState[fromIndex].pieceKind,
                    isPromoted = move.isPromote || boardState[fromIndex].isPromoted // 成駒は維持
                )
            boardState[fromIndex] = PanelState(move.from.row, move.from.column, PieceKind.EMPTY)
        }
    }

    private fun posToIndex(position: Position): Int = position.row * BOARD_SIZE + position.column

    private fun isInside(position: Position): Boolean =
        0 <= posToIndex(position) && posToIndex(position) < BOARD_SIZE * BOARD_SIZE

    private fun isValidMove(move: Move, panelState: PanelState): Boolean {
        return move.to in listLegalMoves(panelState)
    }

    private fun goldMoves(panelState: PanelState): List<Position> {
        val originalRow = panelState.row
        val originalColumn = panelState.column
        return (-1..1).map { dx ->
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
    }

    fun listLegalMoves(panelState: PanelState): List<Position> {
        val originalRow = panelState.row
        val originalColumn = panelState.column
        val results = if (panelState.isEnemy) listOf() else
            when (panelState.pieceKind) {
                // TODO promotion
                PieceKind.EMPTY -> {
                    return listOf()
                }
                PieceKind.PAWN ->
                    when (panelState.isPromoted) {
                        true -> goldMoves(panelState)
                        false -> {
                            listOf(Position(originalRow - 1, originalColumn))
                                .filter {
                                    isInside(it) && (boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY || boardState[posToIndex(
                                        it
                                    )].isEnemy)
                                }
                        }

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
                PieceKind.ROOK -> {
                    val nextPositions = mutableListOf<Position>()
                    for (dir in 0 until 4) {
                        for (length in 1 until BOARD_SIZE) {
                            val nextPosition = when (dir) {
                                0 -> Position(originalRow, originalColumn + length)
                                1 -> Position(originalRow - length, originalColumn)
                                2 -> Position(originalRow, originalColumn - length)
                                else -> Position(originalRow + length, originalColumn)
                            }
                            if (!isInside(nextPosition)) break
                            // 線駒は各方向に自駒に衝突するかはじめに遭遇する敵駒マスまで進める
                            if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY
                                || boardState[posToIndex(nextPosition)].isEnemy
                            ) {
                                nextPositions.add(nextPosition)
                            }
                            if (boardState[posToIndex(nextPosition)].pieceKind != PieceKind.EMPTY) break
                        }
                    }
                    if (panelState.isPromoted) {
                        val directions = listOf(Pair(1, 1), Pair(1, -1), Pair(-1, -1), Pair(-1, 1))
                        val additionalMoves = directions.map {
                            Position(originalRow + it.first, originalColumn + it.second)
                        }.filter {
                            isInside(it) && (
                                    boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY
                                            || boardState[posToIndex(it)].isEnemy)
                        }
                        nextPositions.addAll(additionalMoves)
                    }
                    return nextPositions.toList()
                }
                PieceKind.BISHOP -> {
                    val nextPositions = mutableListOf<Position>()
                    for (dir in 0 until 4) {
                        for (length in 1 until BOARD_SIZE) {
                            val nextPosition = when (dir) {
                                0 -> Position(originalRow - length, originalColumn + length)
                                1 -> Position(originalRow - length, originalColumn - length)
                                2 -> Position(originalRow + length, originalColumn - length)
                                else -> Position(
                                    originalRow + length,
                                    originalColumn + length
                                )
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
                    // promote
                    if (panelState.isPromoted) {
                        val directions = listOf(Pair(1, 0), Pair(-1, 0), Pair(0, -1), Pair(0, 1))
                        val additionalMoves = directions.map {
                            Position(originalRow + it.first, originalColumn + it.second)
                        }.filter {
                            isInside(it) && (
                                    boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY
                                            || boardState[posToIndex(it)].isEnemy)
                        }
                        nextPositions.addAll(additionalMoves)
                    }

                    return nextPositions.toList()
                }
                PieceKind.GOLD -> goldMoves(panelState)
                PieceKind.SILVER -> when (panelState.isPromoted) {
                    false -> listOf(
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
                    true -> goldMoves(panelState)
                }
                PieceKind.KNIGHT -> when (panelState.isPromoted) {
                    true -> goldMoves(panelState)
                    false -> listOf(
                        Position(originalRow - 2, originalColumn + 1),
                        Position(originalRow - 2, originalColumn - 1),
                    ).filter {
                        isInside(it) && (boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY || boardState[posToIndex(
                            it
                        )].isEnemy)
                    }
                }
                PieceKind.LANCE ->
                    when (panelState.isPromoted) {
                        true -> goldMoves(panelState)
                        false -> {
                            val nextPositions = mutableListOf<Position>()
                            for (length in 1 until BOARD_SIZE) {
                                val nextPosition = Position(originalRow - length, originalColumn)
                                if (!isInside(nextPosition)) break
                                if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY
                                    || boardState[posToIndex(nextPosition)].isEnemy
                                ) {
                                    nextPositions.add(nextPosition)
                                }
                                if (boardState[posToIndex(nextPosition)].pieceKind != PieceKind.EMPTY) break
                            }
                            return nextPositions.toList()

                        }
                    }
            }
        return results
    }
}