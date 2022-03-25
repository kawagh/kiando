package com.example.kiando

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kiando.ui.theme.BoardColor
import com.example.kiando.ui.theme.BoardColorUnfocused
import kotlinx.coroutines.launch
import kotlin.concurrent.timerTask

@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen(sampleQuestion)
}


@Composable
fun MainScreen(question: Question) {
    val gameViewModel: GameViewModel = viewModel<GameViewModel>(
        factory = GameViewModelFactory(
            LocalContext.current.applicationContext as Application, question
        )
    )

    // state
//    var questionId by remember {
//        mutableStateOf(question.id)
//    }
    val positionStack = remember {
        mutableStateListOf<Position>()
    }
    var lastClickedPanel = remember {
        PanelState(-1, -1, pieceKind = PieceKind.EMPTY)
    }
    var lastClickedPanelPos by remember {
        mutableStateOf(Position(-1, -1))
    }
    var panelClickedOnce by remember {
        mutableStateOf(false)
    }
    var shouldShowPromotionDialog by remember {
        mutableStateOf(false)
    }
    val scaffoldState = rememberScaffoldState()
    val snackbarCoroutineScope = rememberCoroutineScope()
    val legalMovePositions = remember {
        mutableStateListOf<Position>()
    }
    val handleClearState: () -> Unit = {
        positionStack.clear()
        lastClickedPanelPos = Position(-1, -1)
        panelClickedOnce = false
        legalMovePositions.clear()
//        gameViewModel.loadQuestion(questionId)
    }

    fun processMove(move: Move) {
        // judge
        if (move == question.answerMove) {
            snackbarCoroutineScope.launch {
                scaffoldState.snackbarHostState.showSnackbar(
                    "Good Move👍"
                )
            }
        }
        gameViewModel.move(move)
        positionStack.clear()
        legalMovePositions.clear()
    }

    val handlePanelClick: (PanelState) -> Unit = {
        when (panelClickedOnce) {
            true -> {
                panelClickedOnce = !panelClickedOnce
                positionStack.add(Position(it.row, it.column))
                val move = Move(positionStack.first(), positionStack.last())
                if (gameViewModel.isMoveFromKomadai(move)) {
                    processMove(move)
                } else {
                    // 指し手の確定タイミングは成の余地の有無でDialog前後に分岐する
                    when (gameViewModel.listLegalMoves(lastClickedPanel)
                        .contains(positionStack.last()) && gameViewModel.isPromotable(move)) {
                        true -> {
                            // judge promote here
                            shouldShowPromotionDialog = true
                        }
                        false -> {
                            processMove(move)
                        }
                    }
                }
            }
            false -> {
                panelClickedOnce = !panelClickedOnce
                positionStack.add(Position(it.row, it.column))
                lastClickedPanelPos = Position(it.row, it.column)
                lastClickedPanel = it
                legalMovePositions.addAll(gameViewModel.listLegalMoves(it))
            }
        }
    }

    // komadai
    //    myKomadai:-1,
    //    enemyKomadai:-2,
    val handleKomadaiClick: (PieceKind) -> Unit = {
        when (panelClickedOnce) {
            true -> {
                panelClickedOnce = !panelClickedOnce
            }
            false -> {
                panelClickedOnce = !panelClickedOnce
                positionStack.add(Position(-1, it.ordinal)) // move.fromにpiecekindを埋め込んでいる
                legalMovePositions.addAll(gameViewModel.listLegalMovesFromKomadai(it))
                lastClickedPanelPos = Position(-1, -1) // 駒台を表す
            }
        }
    }
    val handleEnemyKomadaiClick: (PieceKind) -> Unit = {
        when (panelClickedOnce) {
            true -> {
                panelClickedOnce = !panelClickedOnce
            }
            false -> {
                panelClickedOnce = !panelClickedOnce
                positionStack.add(Position(-2, it.ordinal)) // move.fromにpiecekindを埋め込んでいる
                legalMovePositions.addAll(gameViewModel.listLegalMovesFromKomadai(it))
                lastClickedPanelPos = Position(-1, -1) // 駒台を表す
            }
        }
    }

    val piecesCount: Map<PieceKind, Int> = gameViewModel.komadaiState.groupingBy { it }.eachCount()
    val enemyPiecesCount: Map<PieceKind, Int> =
        gameViewModel.enemyKomadaiState.groupingBy { it }.eachCount()

    Scaffold(scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Filled.Share, "share sfen")
                    }
                }
            )
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PromotionDialog(
                    shouldShowPromotionDialog = shouldShowPromotionDialog,
                    onConfirmClick = {
                        shouldShowPromotionDialog = false
                        // processMove
                        val move = Move(
                            positionStack.first(),
                            positionStack.last(),
                            true,
                        )
                        processMove(move)
                    },
                    onDismissClick = {
                        shouldShowPromotionDialog = false
                        val move = Move(
                            positionStack.first(),
                            positionStack.last(),
                            false,
                        )
                        processMove(move)
                    })
                // Debug

                Button(onClick = { gameViewModel.saveQuestion() }) {
                    Text(text = "Save")
                }
                Text(text = SFENConverter().covertTo(gameViewModel.boardState))
                Text(
                    text = "Enemy Komadai:${
                        SFENConverter().convertKomadaiTo(
                            pieceCount = enemyPiecesCount,
                            isOwnedEnemy = true
                        )
                    }"
                )
                Text(
                    text = "My komadai:${
                        SFENConverter().convertKomadaiTo(
                            pieceCount = piecesCount,
                            isOwnedEnemy = false
                        )
                    }"
                )


                // enemy
                Komadai(
                    enemyPiecesCount,
                    handleEnemyKomadaiClick,
                )
                Spacer(modifier = Modifier.size(10.dp))
                Board(
                    gameViewModel.boardState,
                    handlePanelClick,
                    panelClickedOnce,
                    lastClickedPanelPos,
                    legalMovePositions
                )
                Spacer(modifier = Modifier.size(10.dp))
                Komadai(
                    piecesCount,
                    handleKomadaiClick
                )
                Text(text = question.description, fontSize = MaterialTheme.typography.h5.fontSize)

                // FIXME
//                Row() {
//                    Button(
//                        onClick = {
//                            questionId--
//                            handleClearState()
//                        },
//                        enabled = questionId > 0
//                    ) {
//                        Text(text = "prev")
//                    }
//                    Button(
//                        onClick = {
//                            questionId++
//                            handleClearState()
//                        },
//                        enabled = questionId + 1 < sampleQuestions.size
//                    ) {
//                        Text(text = "next")
//                    }
//                }
            }
        }
    )
}

@Composable
private fun PromotionDialog(
    shouldShowPromotionDialog: Boolean,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
) {
    if (shouldShowPromotionDialog) {
        AlertDialog(onDismissRequest = {},
            title = {
                Text(text = "promote?")
            },
            confirmButton = {
                TextButton(onClick = onConfirmClick) {
                    Text(text = "YES")

                }
            },
            dismissButton = {
                TextButton(onClick = onDismissClick) {
                    Text(text = "NO")
                }
            }
        )
    }
}

@Composable
private fun Komadai(
    piecesCount: Map<PieceKind, Int>,
    handleKomadaiClick: (PieceKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = Modifier.background(BoardColorUnfocused)) {
        Row(
            modifier = modifier.width((40 * BOARD_SIZE).dp), // button size
            horizontalArrangement = Arrangement.Center,
        ) {
            piecesCount.forEach { (pieceKind, count) ->
                Button(onClick = { handleKomadaiClick(pieceKind) }) {
                    Piece(pieceKind)
                    Text(text = "x", fontSize = 15.sp)
                    Text(text = "$count")
                }
            }
        }
    }
}

@Composable
private fun Piece(pieceKind: PieceKind) {
    val text = when (pieceKind) {
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
    Text(text = text, fontSize = 17.sp)
}


@Composable
private fun Board(
    boardState: SnapshotStateList<PanelState>,
    handlePanelClick: (PanelState) -> Unit,
    panelClickedOnce: Boolean,
    lastClickedPanelPos: Position,
    legalMovePositions: List<Position>,
) {
    Column(
    ) {
        repeat(BOARD_SIZE) { rowIndex ->
            BoardRow(
                boardState.subList(rowIndex * BOARD_SIZE, rowIndex * BOARD_SIZE + BOARD_SIZE),
                handlePanelClick,
                panelClickedOnce,
                lastClickedPanelPos,
                legalMovePositions
            )
        }
    }
}

@Composable
private fun BoardRow(
    boardRow: List<PanelState>,
    handlePanelClick: (PanelState) -> Unit,
    panelClickedOnce: Boolean,
    lastClickedPanelPos: Position,
    legalMovePositions: List<Position>,
) {
    Row() {
        repeat(BOARD_SIZE) { colIndex ->
            Panel(
                boardRow[colIndex],
                handlePanelClick,
                panelClickedOnce,
                lastClickedPanelPos,
                legalMovePositions
            )

        }
    }
}

@Composable
private fun Panel(
    panelState: PanelState,
    handlePanelClick: (PanelState) -> Unit,
    panelClickedOnce: Boolean,
    lastClickedPanelPos: Position,
    legalMovePositions: List<Position>,
) {
    Button(
        onClick = { handlePanelClick(panelState) },
        modifier = Modifier
            .size(40.dp)
            .border(BorderStroke(0.1.dp, Color.Black)),
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = if (panelClickedOnce) {
                when (Position(panelState.row, panelState.column)) {
                    lastClickedPanelPos -> BoardColor
                    in legalMovePositions -> BoardColor
                    else -> BoardColorUnfocused
                }
            } else {
                BoardColor
            },
            contentColor = Color.Black,
        )
    ) {
        val text = when (panelState.pieceKind) {
            PieceKind.EMPTY -> ""
            PieceKind.KING -> "王"
            PieceKind.ROOK -> if (panelState.isPromoted) "龍" else "飛"
            PieceKind.BISHOP -> if (panelState.isPromoted) "馬" else "角"
            PieceKind.GOLD -> "金"
            PieceKind.SILVER -> if (panelState.isPromoted) "全" else "銀"
            PieceKind.KNIGHT -> if (panelState.isPromoted) "圭" else "桂"
            PieceKind.LANCE -> if (panelState.isPromoted) "杏" else "香"
            PieceKind.PAWN -> if (panelState.isPromoted) "と" else "歩"
        }
        Text(
            text = text, fontSize = 17.sp,
            color = if (panelState.isPromoted) Color.Red else Color.Black,
            modifier = if (panelState.isEnemy) Modifier.rotate(180f) else Modifier,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}