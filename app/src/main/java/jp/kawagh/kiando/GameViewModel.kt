package jp.kawagh.kiando

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.kawagh.kiando.data.Repository
import jp.kawagh.kiando.models.Move
import jp.kawagh.kiando.models.PanelState
import jp.kawagh.kiando.models.PieceKind
import jp.kawagh.kiando.models.Position
import jp.kawagh.kiando.models.Question
import jp.kawagh.kiando.models.fromEnemyKomadai
import jp.kawagh.kiando.models.fromMyKomadai
import jp.kawagh.kiando.network.KiandoApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import java.net.ConnectException

const val BOARD_SIZE = 9

class GameViewModel @AssistedInject constructor(
    private val repository: Repository,
    private val apiService: KiandoApiService,
    @ApplicationContext private val context: Context,
    @Assisted private val question: Question
) :
    ViewModel() {

    @AssistedFactory
    interface GameViewModelAssistedFactory {
        fun create(
            question: Question
        ): GameViewModel
    }

    var boardState: SnapshotStateList<PanelState> = question.boardState.toMutableStateList()
    var komadaiState: SnapshotStateList<PieceKind> = question.myKomadai.toMutableStateList()
    var enemyKomadaiState: SnapshotStateList<PieceKind> = question.enemyKomadai.toMutableStateList()
    fun uploadImage(uri: Uri) {
        val file = getFileFromContentUri(uri) ?: return
        val requestFile: RequestBody = file.asRequestBody("image/png".toMediaType())
        val imagePart: MultipartBody.Part =
            MultipartBody.Part.createFormData("file", file.name, requestFile)
        viewModelScope.launch {
            try {
                val result = apiService.getSFENResponse(imagePart)
                if (result.isSuccessful) {
                    Timber.d(result.body()!!.sfen)
                    loadSFEN(result.body()!!.sfen)
                }
            } catch (e: ConnectException) {
                Timber.d(e.message)
            }
        }
    }

    fun saveQuestion(question: Question) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.add(question)
        }
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
                        !boardState[fromIndex].isPromoted &&
                                (move.from.row >= BOARD_SIZE - 3 || move.to.row >= BOARD_SIZE - 3)

                    false ->
                        !boardState[fromIndex].isPromoted && (move.from.row < 3 || move.to.row < 3)
                }
        }
    }

    // 不成で進む先の無い手は成るしかない
    fun mustPromote(move: Move): Boolean {
        if (isMoveFromKomadai(move)) {
            return false
        }
        val fromIndex = move.from.row * BOARD_SIZE + move.from.column
        return when (boardState[fromIndex].pieceKind) {
            PieceKind.KNIGHT ->
                (!boardState[fromIndex].isEnemy && move.to.row < 2) ||
                        (boardState[fromIndex].isEnemy && move.to.row >= BOARD_SIZE - 2)

            PieceKind.LANCE -> (!boardState[fromIndex].isEnemy && move.to.row == 0) ||
                    (boardState[fromIndex].isEnemy && move.to.row == BOARD_SIZE - 1)

            PieceKind.PAWN -> (!boardState[fromIndex].isEnemy && move.to.row == 0) ||
                    (boardState[fromIndex].isEnemy && move.to.row == BOARD_SIZE - 1)

            else -> false
        }
    }

    private fun isMoveFromKomadai(move: Move): Boolean =
        move.fromMyKomadai() || move.fromEnemyKomadai()

    fun move(move: Move) {
        val fromIndex = move.from.row * BOARD_SIZE + move.from.column
        val toIndex = move.to.row * BOARD_SIZE + move.to.column
        if (fromIndex == toIndex) return
        // 駒台からの打ち込み
        if (isMoveFromKomadai(move)) {
            val pieceKind: PieceKind = PieceKind.values()[move.from.column]
            if (!listLegalMovesFromKomadai(pieceKind, isEnemy = move.fromEnemyKomadai()).contains(
                    move.to
                )
            ) {
                Timber.d("not legal move")
                return
            }
            if (move.fromMyKomadai()) {
                boardState[toIndex] =
                    PanelState(move.to.row, move.to.column, pieceKind, isEnemy = false)
                komadaiState.remove(pieceKind)
            } else if (move.fromEnemyKomadai()) {
                boardState[toIndex] =
                    PanelState(move.to.row, move.to.column, pieceKind, isEnemy = true)
                enemyKomadaiState.remove(pieceKind)
            }
        } else if (isValidMove(move, boardState[fromIndex])) {
            if (boardState[toIndex].pieceKind != PieceKind.EMPTY) {
                if (boardState[toIndex].isEnemy) {
                    komadaiState.add(boardState[toIndex].pieceKind)
                } else {
                    enemyKomadaiState.add(boardState[toIndex].pieceKind)
                }
            }
            boardState[toIndex] =
                PanelState(
                    move.to.row,
                    move.to.column,
                    boardState[fromIndex].pieceKind,
                    isEnemy = boardState[fromIndex].isEnemy,
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
                    boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY ||
                            (boardState[posToIndex(it)].isEnemy.xor(panelState.isEnemy)) // 敵対している駒か
                    )
        }
    }

    fun listLegalMovesFromKomadai(pieceKind: PieceKind, isEnemy: Boolean): List<Position> {
        // TODO 進行方向なしの考慮
        if (pieceKind == PieceKind.PAWN) {
            // 自陣営の歩(と金は除く)の存在する筋を保持する
            val linesWithPawn = mutableSetOf<Int>()
            for (index in 0 until BOARD_SIZE * BOARD_SIZE) {
                if (boardState[index].pieceKind == PieceKind.PAWN &&
                    boardState[index].isEnemy == isEnemy &&
                    !boardState[index].isPromoted
                ) {
                    linesWithPawn.add(index % BOARD_SIZE)
                }
            }
            return List(BOARD_SIZE * BOARD_SIZE) {
                if (linesWithPawn.contains(it % BOARD_SIZE)) {
                    null
                } else {
                    it
                }
            }.filterNotNull()
                .map {
                    Position(it / BOARD_SIZE, it % BOARD_SIZE)
                }.filter { boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY }
        } else {
            return List(BOARD_SIZE * BOARD_SIZE) {
                Position(it / BOARD_SIZE, it % BOARD_SIZE)
            }.filter { boardState[posToIndex(it)].pieceKind == PieceKind.EMPTY }
        }
    }

    fun listLegalMoves(panelState: PanelState): List<Position> {
        val originalRow = panelState.row
        val originalColumn = panelState.column
        // negate offset
        return when (panelState.pieceKind) {
            PieceKind.EMPTY -> emptyList()

            PieceKind.PAWN -> {
                if (panelState.isPromoted) {
                    goldMoves(panelState)
                } else {
                    val sign = if (panelState.isEnemy) -1 else 1
                    listOf(Position(originalRow - sign, originalColumn))
                        .filterMovable(panelState)
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
                        // 線駒は各方向に自駒に衝突するかはじめに遭遇する敵駒マスまで進める
                        if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY ||
                            boardState[posToIndex(nextPosition)].isEnemy.xor(panelState.isEnemy)
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
                nextPositions.toList()
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

                        if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY ||
                            (boardState[posToIndex(nextPosition)].isEnemy.xor(panelState.isEnemy))
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
                nextPositions.toList()
            }

            PieceKind.GOLD -> goldMoves(panelState)
            PieceKind.SILVER -> if (panelState.isPromoted) {
                goldMoves(panelState)
            } else {
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

            PieceKind.KNIGHT -> if (panelState.isPromoted) {
                goldMoves(panelState)
            } else {
                val sign = if (panelState.isEnemy) -1 else 1
                listOf(
                    Position(originalRow - sign * 2, originalColumn + 1),
                    Position(originalRow - sign * 2, originalColumn - 1),
                ).filterMovable(panelState)
            }

            PieceKind.LANCE ->
                if (panelState.isPromoted) {
                    goldMoves(panelState)
                } else {
                    val nextPositions = mutableListOf<Position>()
                    val sign = if (panelState.isEnemy) -1 else 1
                    for (length in 1 until BOARD_SIZE) {
                        val nextPosition = Position(originalRow - sign * length, originalColumn)
                        if (!isInside(nextPosition)) break
                        if (boardState[posToIndex(nextPosition)].pieceKind == PieceKind.EMPTY ||
                            boardState[posToIndex(nextPosition)].isEnemy.xor(panelState.isEnemy)
                        ) {
                            nextPositions.add(nextPosition)
                        }
                        if (boardState[posToIndex(nextPosition)].pieceKind != PieceKind.EMPTY) break
                    }
                    nextPositions.toList()
                }
        }
    }

    // file utility
    private fun getFileFromContentUri(uri: Uri): File? {
        var file: File? = null
        val filePath: String?

        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val fileName = it.getString(displayNameIndex)

                    // make directory to save file
                    val cacheDir = context.cacheDir
                    val tempFile = File(cacheDir, fileName)

                    // copy file
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        tempFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    filePath = tempFile.absolutePath
                    if (filePath == null) {
                        return null
                    }
                    file = File(filePath)
                }
            }
        }

        return file
    }
}
