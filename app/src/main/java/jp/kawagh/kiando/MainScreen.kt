package jp.kawagh.kiando

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.kawagh.kiando.ui.components.Komadai
import jp.kawagh.kiando.ui.components.Piece
import jp.kawagh.kiando.ui.theme.BoardColor
import jp.kawagh.kiando.ui.theme.BoardColorUnfocused
import kotlinx.coroutines.launch


@Preview
@Composable
private fun MainScreenPreview() {
    MainScreen(sampleQuestion, {}, {}, {})
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    question: Question, navigateToList: () -> Unit,
    navigateToNextQuestion: () -> Unit,
    navigateToPrevQuestion: () -> Unit,
) {
    val gameViewModel: GameViewModel = viewModel(
        factory = GameViewModelFactory(
            LocalContext.current.applicationContext as Application, question
        )
    )
    val snackbarHostState = remember { SnackbarHostState() }
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
    var shouldShowSFENInput by remember {
        mutableStateOf(false)
    }
    var shouldShowAnswerButton by remember {
        mutableStateOf(true)
    }
    var showAnswerMode by remember {
        mutableStateOf(false)
    }
    val snackbarCoroutineScope = rememberCoroutineScope()
    val positionsToHighlight = remember {
        mutableStateListOf<Position>()
    }

    fun registerMove(move: Move) {
        moveToRegister = move
        gameViewModel.move(move)
        positionStack.clear()
        positionsToHighlight.clear()
    }

    fun processMove(move: Move) {
        showAnswerMode = false
        // judge
        if (move == question.answerMove) {
            snackbarCoroutineScope.launch {
                val snackbarResult =
                    snackbarHostState.showSnackbar(message = "Good Move👍", actionLabel = "Next")

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
        positionsToHighlight.clear()
    }

    val handlePanelClick: (PanelState) -> Unit = {
        shouldShowAnswerButton = false
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
                positionsToHighlight.addAll(gameViewModel.listLegalMoves(it))
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
                positionsToHighlight.addAll(gameViewModel.listLegalMovesFromKomadai(it))
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
                positionsToHighlight.addAll(gameViewModel.listLegalMovesFromKomadai(it))
                lastClickedPanelPos = Position(-1, -1) // 駒台を表す
            }
        }
    }

    val handleShowAnswerClick: () -> Unit = {
        showAnswerMode = true
        shouldShowAnswerButton = false

        positionsToHighlight.addAll(listOf(question.answerMove.from, question.answerMove.to))
    }

    val piecesCount: Map<PieceKind, Int> = gameViewModel.komadaiState.groupingBy { it }.eachCount()
    val enemyPiecesCount: Map<PieceKind, Int> =
        gameViewModel.enemyKomadaiState.groupingBy { it }.eachCount()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                        shouldShowSFENInput = !shouldShowSFENInput
                    }) {
                        Icon(Icons.Filled.TextRotateVertical, "toggle decode SFEN input form")
                    }
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
                                snackbarHostState.showSnackbar(
                                    "🆖 empty description"
                                )
                            }
                        }
                        QuestionValidationResults.NeedMove -> {
                            snackbarCoroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    "🆖 need move"
                                )
                            }

                        }
                        QuestionValidationResults.Valid -> {
                            gameViewModel.saveQuestion(newQuestion)
                            snackbarCoroutineScope.launch {
                                snackbarHostState.showSnackbar(
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                )
            }
            if (shouldShowSFENInput) {
                val clipboardManager = LocalClipboardManager.current
                Row {
                    TextField(
                        value = inputSFEN,
                        label = { Text("SFEN") },
                        placeholder = { Text("Input SFEN") },
                        onValueChange = { inputSFEN = it },
                        trailingIcon = {
                            Row {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(buildAnnotatedString {
                                            append(
                                                inputSFEN
                                            )
                                        }
                                        )
                                        snackbarCoroutineScope.launch {
                                            snackbarHostState.showSnackbar("copied $inputSFEN")
                                        }
                                    },
                                    enabled = inputSFEN.isNotEmpty(),
                                ) {
                                    Icon(
                                        Icons.Filled.ContentCopy, null,
                                        tint = if (inputSFEN.isNotEmpty()) BoardColor else Color.Gray
                                    )

                                }
                                IconButton(
                                    onClick = {
                                        gameViewModel.loadSFEN(inputSFEN)
                                        isRegisterQuestionMode = false
                                        isRegisterQuestionMode = true // invoke recompose
                                        inputKomadaiSFEN = "" // TODO parse komadai from sfen
                                    },
                                    enabled = inputSFEN.isNotEmpty(),
                                ) {
                                    Icon(
                                        Icons.Filled.Sync, "load SFEN",
                                        tint = if (inputSFEN.isNotEmpty()) BoardColor else Color.Gray,
                                    )
                                }
                            }
                        },
                        modifier = Modifier.semantics { contentDescription = "SFEN input form" }
                    )
                }
            }

            // enemy
            Komadai(
                enemyPiecesCount,
                handleEnemyKomadaiClick,
                isEnemy = true,
            )
            Spacer(modifier = Modifier.size(10.dp))
            Board(
                gameViewModel.boardState,
                handlePanelClick,
                shouldHighlight = panelClickedOnce || showAnswerMode,
                lastClickedPanelPos,
                positionsToHighlight = positionsToHighlight
            )
            Spacer(modifier = Modifier.size(10.dp))
            Komadai(
                piecesCount,
                handleKomadaiClick
            )
            when (isRegisterQuestionMode) {
                false -> Column() {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = question.description,
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            modifier = Modifier
                                .weight(1f)
                                .padding(20.dp)
                        )
                        IconButton(
                            onClick = {
                                navigateToPrevQuestion.invoke()
                            },
                        ) {
                            Icon(Icons.Default.SkipPrevious, "back to prev question")
                        }
                        IconButton(
                            onClick = {
                                navigateToNextQuestion.invoke()
                            },
                            modifier = Modifier.padding(10.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, "go to next question")
                        }
                    }
                    if (shouldShowAnswerButton) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(onClick = handleShowAnswerClick) {
                                Text(text = "show answer")
                            }
                        }
                    }
                }
                true -> TextField(
                    value = inputQuestionDescription,
                    onValueChange = { inputQuestionDescription = it },
                    label = {
                        Text(text = "Input question description")
                    },
                )
            }
        }
    }
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
private fun Board(
    boardState: SnapshotStateList<PanelState>,
    handlePanelClick: (PanelState) -> Unit,
    shouldHighlight: Boolean,
    lastClickedPanelPos: Position,
    positionsToHighlight: List<Position>,
) {
    Column {
        repeat(BOARD_SIZE) { rowIndex ->
            BoardRow(
                boardState.subList(rowIndex * BOARD_SIZE, rowIndex * BOARD_SIZE + BOARD_SIZE),
                handlePanelClick,
                shouldHighlight,
                lastClickedPanelPos,
                positionsToHighlight
            )
        }
    }
}

@Composable
private fun BoardRow(
    boardRow: List<PanelState>,
    handlePanelClick: (PanelState) -> Unit,
    shouldHighlight: Boolean,
    lastClickedPanelPos: Position,
    positionsToHighlight: List<Position>,
) = Row {
    repeat(BOARD_SIZE) { colIndex ->
        Panel(
            boardRow[colIndex],
            handlePanelClick,
            shouldHighlight,
            lastClickedPanelPos,
            positionsToHighlight
        )

    }
}

@Composable
private fun Panel(
    panelState: PanelState,
    handlePanelClick: (PanelState) -> Unit,
    shouldHighlight: Boolean,
    lastClickedPanelPos: Position,
    positionsToHighlight: List<Position>,
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
    val backgroundColor = if (shouldHighlight) {
        when (Position(panelState.row, panelState.column)) {
            lastClickedPanelPos -> BoardColor
            in positionsToHighlight -> BoardColor
            else -> BoardColorUnfocused
        }
    } else BoardColorUnfocused


    when (panelState.pieceKind) {
        PieceKind.EMPTY -> {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(backgroundColor)
                    .clickable { handlePanelClick.invoke(panelState) }
                    .border(BorderStroke(0.1.dp, Color.Black))
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