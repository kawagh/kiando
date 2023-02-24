package jp.kawagh.kiando

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TextRotateVertical
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.ui.components.Board
import jp.kawagh.kiando.ui.components.Komadai
import jp.kawagh.kiando.ui.theme.BoardColor
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
    gameViewModel: GameViewModel,
    question: Question, navigateToList: () -> Unit,
    navigateToNextQuestion: () -> Unit,
    navigateToPrevQuestion: () -> Unit,
) {

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
                title = {
                    Text(
                        text = if (isRegisterQuestionMode) {
                            "問題登録"
                        } else {
                            question.description
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = navigateToList) {
                        Icon(Icons.Filled.ArrowBack, "back to the list")
                    }
                },
                actions = {
                    if (isRegisterQuestionMode) {
                        IconToggleButton(
                            checked = shouldShowSFENInput,
                            onCheckedChange = { shouldShowSFENInput = !shouldShowSFENInput }) {
                            Icon(Icons.Filled.TextRotateVertical, "toggle decode SFEN input form")

                        }
                    }
                    IconButton(onClick = {
                        if (!isRegisterQuestionMode) {
                            moveToRegister = NonMove
                        } else {
                            shouldShowSFENInput = false
                        }
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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Top,
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

            val clipboardManager = LocalClipboardManager.current
            val context = LocalContext.current
            Spacer(modifier = Modifier.size(8.dp))
            AnimatedVisibility(visible = shouldShowSFENInput) {
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
                                            snackbarHostState
                                                .showSnackbar(
                                                    context.getString(R.string.sfen_copy)
                                                )
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
                                        snackbarCoroutineScope.launch {
                                            snackbarHostState
                                                .showSnackbar(
                                                    context.getString(R.string.sfen_load)
                                                )
                                        }
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
                        modifier = Modifier
                            .semantics { contentDescription = "SFEN input form" }
                            .padding(horizontal = 16.dp)
                    )
                }
            }
            when (isRegisterQuestionMode) {
                true -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (moveToRegister == NonMove) {
                            "登録する手を指してください"
                        } else {
                            val pieceKind =
                                gameViewModel.boardState[moveToRegister.to.row * BOARD_SIZE
                                        + moveToRegister.to.column].pieceKind
                            "登録手: ${moveToRegister.toReadable(pieceKind)}"
                        },
                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                    )
                    val keyboardController = LocalSoftwareKeyboardController.current
                    OutlinedTextField(
                        value = inputQuestionDescription,
                        onValueChange = { inputQuestionDescription = it },
                        label = {
                            Text(text = "問題名")
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                val newQuestion = Question(
                                    id = 0,
                                    description = inputQuestionDescription,
                                    answerMove = moveToRegister,
                                    sfen = inputSFEN,
                                    komadaiSfen = inputKomadaiSFEN,
                                )
                                when (validateQuestion(newQuestion)) {
                                    QuestionValidationResults.EmptyDescription -> {
                                        keyboardController?.hide() // to avoid keyboard on snackbar
                                        snackbarCoroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "🆖 empty description"
                                            )
                                        }
                                    }

                                    QuestionValidationResults.NeedMove -> {
                                        keyboardController?.hide()
                                        snackbarCoroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                "🆖 need move"
                                            )
                                        }

                                    }

                                    QuestionValidationResults.Valid -> {
                                        keyboardController?.hide()
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
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "register question"
                                )
                            }
                        }
                    )
                }

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

