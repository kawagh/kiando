package jp.kawagh.kiando

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.runtime.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.kawagh.kiando.ui.components.Piece
import jp.kawagh.kiando.ui.theme.BoardColor
import jp.kawagh.kiando.ui.theme.BoardColorUnfocused
import kotlinx.coroutines.launch


@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen(sampleQuestion, {}, {})
}


@Composable
fun MainScreen(
    question: Question, navigateToList: () -> Unit,
    navigateToNextQuestion: () -> Unit,
) {
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(
            LocalContext.current.applicationContext as Application, question
        )
    )
    var isRegisterQuestionMode by remember {
        mutableStateOf(false)
    }
    var inputSFEN by remember {
        mutableStateOf("")
    }
    var inputKomadaiSFEN by remember {
        mutableStateOf("")
    }
    var inputQuestionDescription by remember {
        mutableStateOf("")
    }
    var moveToRegister by remember {
        mutableStateOf(NonMove)
    }


    // state
    val positionStack = remember {
        mutableStateListOf<Position>()
    }
    var lastClickedPanel = remember {
        PanelState(-1, -1, pieceKind = PieceKind.EMPTY)
    }
    var lastClickedPanelPos by remember {
        mutableStateOf(NonPosition)
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

    fun registerMove(move: Move) {
        moveToRegister = move
        gameViewModel.move(move)
        positionStack.clear()
        legalMovePositions.clear()
    }

    fun processMove(move: Move) {
        // judge
        if (move == question.answerMove) {
            snackbarCoroutineScope.launch {
                val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                    message = "Good Move👍",
                    actionLabel = "Next",
                )
                when (snackbarResult) {
                    SnackbarResult.ActionPerformed -> {
                        navigateToNextQuestion.invoke()
                    }
                    SnackbarResult.Dismissed -> {}
                }
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
                    when (isRegisterQuestionMode) {
                        false -> processMove(move)
                        true -> registerMove(move)
                    }
                } else {
                    // 指し手の確定タイミングは成の余地の有無でDialog前後に分岐する
                    when (gameViewModel.listLegalMoves(lastClickedPanel)
                        .contains(positionStack.last()) && gameViewModel.isPromotable(move)) {
                        true -> {
                            // judge promote here
                            shouldShowPromotionDialog = true
                        }
                        false -> {
                            when (isRegisterQuestionMode) {
                                false -> processMove(move)
                                true -> registerMove(move)
                            }
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

    // komadaik
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
                navigationIcon = {
                    IconButton(onClick = navigateToList) {
                        Icon(Icons.Filled.ArrowBack, "back to the list")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isRegisterQuestionMode = !isRegisterQuestionMode
                        // modeに入った時点の局面を保持する
                        if (isRegisterQuestionMode) {
                            inputSFEN = SFENConverter().covertTo(gameViewModel.boardState)
                            inputKomadaiSFEN = SFENConverter().convertKomadaiTo(
                                piecesCount,
                                false
                            ) + SFENConverter().convertKomadaiTo(enemyPiecesCount, true)
                        }
                    }) {
                        when (isRegisterQuestionMode) {
                            false -> Icon(Icons.Filled.Add, "enter in registering Question")
                            true -> Icon(Icons.Filled.Cancel, "cancel registering ")
                        }
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

                        when (isRegisterQuestionMode) {
                            false -> processMove(move)
                            true -> registerMove(move)
                        }
                    },
                    onDismissClick = {
                        shouldShowPromotionDialog = false
                        val move = Move(
                            positionStack.first(),
                            positionStack.last(),
                            false,
                        )
                        when (isRegisterQuestionMode) {
                            false -> processMove(move)
                            true -> registerMove(move)
                        }
                    })
                if (isRegisterQuestionMode) {
                    Text(
                        text = "Do move to register",
                        fontSize = MaterialTheme.typography.h5.fontSize
                    )
                }
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
                when (isRegisterQuestionMode) {
                    false -> Text(
                        text = question.description,
                        fontSize = MaterialTheme.typography.h5.fontSize
                    )
                    true -> TextField(
                        value = inputQuestionDescription,
                        onValueChange = { inputQuestionDescription = it },
                        label = {
                            Text(text = "Input question description")
                        },
                    )
                }
            }
        },
        floatingActionButton = {
            if (isRegisterQuestionMode) {
                val newQuestion = Question(
                    id = 0,
                    description = inputQuestionDescription,
                    answerMove = moveToRegister,
                    sfen = inputSFEN,
                    komadaiSfen = inputKomadaiSFEN,
                )
                FloatingActionButton(onClick = {
                    when (validateQuestion(newQuestion)) {
                        QuestionValidationResults.EmptyDescription -> {
                            snackbarCoroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "🆖 empty description"
                                )
                            }
                        }
                        QuestionValidationResults.NeedMove -> {
                            snackbarCoroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "🆖 need move"
                                )
                            }

                        }
                        QuestionValidationResults.Valid -> {
                            gameViewModel.saveQuestion(newQuestion)
                            snackbarCoroutineScope.launch {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    "🆗 saved"
                                )
                            }
                            isRegisterQuestionMode = false
                            moveToRegister = NonMove
                            inputQuestionDescription = ""
                        }
                    }
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "register new Question")
                }
            }
        }
    )
}

sealed class QuestionValidationResults {
    object Valid : QuestionValidationResults()
    object EmptyDescription : QuestionValidationResults()
    object NeedMove : QuestionValidationResults()
}

fun validateQuestion(question: Question): QuestionValidationResults =
    if (question.description.isEmpty()) {
        QuestionValidationResults.EmptyDescription
    } else {
        when (question.answerMove == NonMove) {
            true -> QuestionValidationResults.NeedMove
            false -> QuestionValidationResults.Valid
        }
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
) {
    val pieceKindMap: Map<PieceKind, String> = mapOf(
        PieceKind.EMPTY to "",
        PieceKind.KING to "王",
        PieceKind.ROOK to "飛",
        PieceKind.BISHOP to "角",
        PieceKind.GOLD to "金",
        PieceKind.SILVER to "銀",
        PieceKind.KNIGHT to "桂",
        PieceKind.LANCE to "香",
        PieceKind.PAWN to "歩",
    )
    Box(
        modifier = Modifier
            .background(BoardColorUnfocused)
            .width((40 * BOARD_SIZE).dp)
            .height(40.dp)
    ) {
        LazyRow {
            items(piecesCount.keys.toList()) { pieceKind ->
                Button(
                    onClick = { handleKomadaiClick(pieceKind) },
                    colors = ButtonDefaults.textButtonColors(
                        backgroundColor = BoardColor,
                        contentColor = Color.Black,
                    ),
                    modifier = Modifier.width(70.dp)
                ) {
                    Text(text = pieceKindMap[pieceKind]!!)
                    Text(text = "x", fontSize = 15.sp)
                    Text(text = "${piecesCount[pieceKind]}")
                }
            }
        }
    }
}


@Composable
private fun Board(
    boardState: SnapshotStateList<PanelState>,
    handlePanelClick: (PanelState) -> Unit,
    panelClickedOnce: Boolean,
    lastClickedPanelPos: Position,
    legalMovePositions: List<Position>,
) {
    Column {
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
) = Row {
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

@Composable
private fun Panel(
    panelState: PanelState,
    handlePanelClick: (PanelState) -> Unit,
    panelClickedOnce: Boolean,
    lastClickedPanelPos: Position,
    legalMovePositions: List<Position>,
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
    val backgroundColor = if (panelClickedOnce) {
        when (Position(panelState.row, panelState.column)) {
            lastClickedPanelPos -> BoardColor
            in legalMovePositions -> BoardColor
            else -> BoardColorUnfocused
        }
    } else BoardColor


    when (panelState.pieceKind) {
        PieceKind.EMPTY -> {
            Button(
                onClick = { handlePanelClick(panelState) },
                modifier = Modifier
                    .size(40.dp)
                    .border(BorderStroke(0.1.dp, Color.Black)),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = backgroundColor,
                    contentColor = Color.Black,
                )
            ) {
                Text(
                    text = text, fontSize = 17.sp,
                    color = if (panelState.isPromoted) Color.Red else Color.Black,
                    modifier = if (panelState.isEnemy) Modifier.rotate(180f) else Modifier,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
        else -> {
            Piece(
                text = text,
                onClick = { handlePanelClick(panelState) },
                isEnemy = panelState.isEnemy,
                isPromoted = panelState.isPromoted,
                modifier = Modifier
                    .size(40.dp)
                    .background(backgroundColor)
                    .border(BorderStroke(0.1.dp, Color.Black))
            )
        }
    }
}