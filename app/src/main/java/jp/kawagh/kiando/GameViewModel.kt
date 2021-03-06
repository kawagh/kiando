package jp.kawagh.kiando

import android.app.Application
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

const val BOARD_SIZE = 9

class GameViewModelFactory(private val application: Application, private val question: Question) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameViewModel(application, question) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}

class GameViewModel(application: Application, question: Question) : AndroidViewModel(application) {
    private val db: AppDatabase = AppDatabase.getInstance(application)

    var boardState: SnapshotStateList<PanelState> = question.boardState.toMutableStateList()
    var komadaiState: SnapshotStateList<PieceKind> = question.myKomadai.toMutableStateList()
    var enemyKomadaiState: SnapshotStateList<PieceKind> = question.enemyKomadai.toMutableStateList()

    fun saveQuestion(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            db.questionDao().insert(question)
        }
    }


    fun loadQuestion(questionId: Int) {
//        boardState = db.qustionDao().findById(questionId).boardState.toMutableStateList()
        boardState = sampleQuestions[questionId].boardState.toMutableStateList()
        komadaiState = listOf<PieceKind>().toMutableStateList()

    }

    fun loadSFEN(sfen: String) {
        boardState = SFENConverter().convertFrom(sfen).toMutableStateList()
        komadaiState = emptyList<PieceKind>().toMutableStateList()
        enemyKomadaiState = emptyList<PieceKind>().toMutableStateList()
    }

    fun isPromotable(move: Move): Boolean {
        val fromIndex = move.from.row * BOARD_SIZE + move.from.column
        return when (boardState[fromIndex].pieceKind) {
            PieceKind.KING -> false
            PieceKind.GOLD -> false
            PieceKind.EMPTY -> false
            else ->
                when (boardState[fromIndex].isEnemy) {
                    true ->
                        !boardState[fromIndex].isPromoted && (move.from.row >= BOARD_SIZE - 3 || move.to.row >= BOARD_SIZE - 3)
                    false ->
                        !boardState[fromIndex].isPromoted && (move.from.row < 3 || move.to.row < 3)
                }
        }
    }

    fun isMoveFromKomadai(move: Move): Boolean = move.from.row < 0
    fun move(move: Move) {
        val fromIndex = move.from.row * BOARD_SIZE + move.from.column
        val toIndex = move.to.row * BOARD_SIZE + move.to.column
        if (fromIndex == toIndex) return
        // ???????????????????????????
        if (isMoveFromKomadai(move)) {
            val pieceKind: PieceKind = PieceKind.values()[move.from.column]
            if (boardState[posToIndex(move.to)].pieceKind != PieceKind.EMPTY) return
            when (move.from.row) {
                -1 -> {
                    boardState[toIndex] =
                        PanelState(move.to.row, move.to.column, pieceKind, isEnemy = false)
                    komadaiState.remove(pieceKind)

                }
                -2 -> {
                    enemyKomadaiState.remove(pieceKind)
                    boardState[toIndex] =
                        PanelState(move.to.row, move.to.column, pieceKind, isEnemy = true)

                }
            }
            return
        }

        val panelState = boardState[fromIndex]
        if (isValidMove(move, panelState)) {
            if (boardState[toIndex].pieceKind != PieceKind.EMPTY) {
                when (boardState[toIndex].isEnemy) {
                    true -> komadaiState.add(boardState[toIndex].pieceKind)
                    false -> enemyKomadaiState.add(boardState[toIndex].pieceKind)
                }
            }
            boardState[toIndex] =
                PanelState(
                    move.to.row,
                    move.to.column,
                    boardState[fromIndex].pieceKind,
                    isEnemy = boardState[fromIndex].isEnemy,
                    isPromoted = move.isPromote || boardState[fromIndex].isPromoted // ???????????????
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
        val sign = if (panelState.isEnemy) -1 else 1
        val offsets = listOf(
            Pair(-1, -1),
            Pair(-1, 0),
            Pair(-1, 1),
            Pair(0, -1),
            Pair(0, 1),
            Pair(1, 0),
        )

        return offsets.map {
            Position(originalRow + sign * it.first, originalColumn + sign * it.second)
        }.filterMovable(panelState)
    }

    private fun List<Position>.filterMovable(panelState: PanelState): List<Position> {
        return this.filter {
            isInside(it) && (
                    boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY
                            || (boardState[posToIndex(it)].isEnemy.xor(panelState.isEnemy)) // ????????????????????????
                    )
        }
    }

    fun listLegalMovesFromKomadai(pieceKind: PieceKind): List<Position> =
        // TODO ??????,???????????????????????????
        List(BOARD_SIZE * BOARD_SIZE) {
            Position(it / BOARD_SIZE, it % BOARD_SIZE)
        }.filter { boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY }

    fun listLegalMoves(panelState: PanelState): List<Position> {
        val originalRow = panelState.row
        val originalColumn = panelState.column
        // negate offset
        val results = when (panelState.pieceKind) {
            PieceKind.EMPTY -> {
                return listOf()
            }
            PieceKind.PAWN ->
                when (panelState.isPromoted) {
                    true -> goldMoves(panelState)
                    false -> {
                        when (panelState.isEnemy) {
                            true -> listOf(Position(originalRow + 1, originalColumn))
                                .filterMovable(panelState)

                            false -> listOf(Position(originalRow - 1, originalColumn))
                                .filterMovable(panelState)
                        }
                    }
                }
            PieceKind.KING -> (-1..1).map { dx ->
                (-1..1).map { dy ->
                    Position(originalRow + dx, originalColumn + dy)
                }
            }.flatten()
                .filterMovable(panelState)
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
                        // ????????????????????????????????????????????????????????????????????????????????????????????????
                        if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY
                            || boardState[posToIndex(nextPosition)].isEnemy.xor(panelState.isEnemy)
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
                    }.filterMovable(panelState)
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
                            || (boardState[posToIndex(nextPosition)].isEnemy.xor(panelState.isEnemy))
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
                    }.filterMovable(panelState)
                    nextPositions.addAll(additionalMoves)
                }

                return nextPositions.toList()
            }
            PieceKind.GOLD -> goldMoves(panelState)
            PieceKind.SILVER -> when (panelState.isPromoted) {
                false -> {
                    val sign = if (panelState.isEnemy) -1 else 1
                    listOf(
                        Position(originalRow - sign * 1, originalColumn - 1),
                        Position(originalRow - sign * 1, originalColumn),
                        Position(originalRow - sign * 1, originalColumn + 1),
                        Position(originalRow + sign * 1, originalColumn + 1),
                        Position(originalRow + sign * 1, originalColumn - 1),
                    )
                        .filterMovable(panelState)
                }
                true -> goldMoves(panelState)
            }
            PieceKind.KNIGHT -> when (panelState.isPromoted) {
                true -> goldMoves(panelState)
                false -> {
                    val sign = if (panelState.isEnemy) -1 else 1
                    listOf(
                        Position(originalRow - sign * 2, originalColumn + 1),
                        Position(originalRow - sign * 2, originalColumn - 1),
                    ).filterMovable(panelState)
                }
            }
            PieceKind.LANCE ->
                when (panelState.isPromoted) {
                    true -> goldMoves(panelState)
                    false -> {
                        val nextPositions = mutableListOf<Position>()
                        val sign = if (panelState.isEnemy) -1 else 1
                        for (length in 1 until BOARD_SIZE) {
                            val nextPosition = Position(originalRow - sign * length, originalColumn)
                            if (!isInside(nextPosition)) break
                            if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY
                                || boardState[posToIndex(nextPosition)].isEnemy.xor(panelState.isEnemy)
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