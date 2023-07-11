package jp.kawagh.kiando.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Screenshot
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TextRotateVertical
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.BOARD_SIZE
import jp.kawagh.kiando.BuildConfig
import jp.kawagh.kiando.GameViewModel
import jp.kawagh.kiando.R
import jp.kawagh.kiando.SFENConverter
import jp.kawagh.kiando.models.ENEMY_KOMADAI_INDEX
import jp.kawagh.kiando.models.MY_KOMADAI_INDEX
import jp.kawagh.kiando.models.Move
import jp.kawagh.kiando.models.NonMove
import jp.kawagh.kiando.models.NonPosition
import jp.kawagh.kiando.models.PanelState
import jp.kawagh.kiando.models.PieceKind
import jp.kawagh.kiando.models.Position
import jp.kawagh.kiando.models.Question
import jp.kawagh.kiando.models.toReadable
import jp.kawagh.kiando.ui.components.Board
import jp.kawagh.kiando.ui.components.Komadai
import jp.kawagh.kiando.ui.components.VisibleIf
import jp.kawagh.kiando.ui.theme.BoardColor
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
    gameViewModel: GameViewModel,
    question: Question,
    navigateToList: () -> Unit,
    navigateToNextQuestion: () -> Unit,
    navigateToPrevQuestion: () -> Unit,
    restartQuestion: () -> Unit,
) {
    val uiState = gameViewModel.uiState
    val context = LocalContext.current
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
    var isAnswerDescriptionEditMode by remember {
        mutableStateOf(false)
    }
    var answerDescriptionTextInput by remember {
        mutableStateOf("")
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
    val scope = rememberCoroutineScope()
    val positionsToHighlight = remember {
        mutableStateListOf<Position>()
    }
    val bottomSheetState =
        rememberStandardBottomSheetState(initialValue = SheetValue.Hidden, skipHiddenState = false)
    val scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) {
            Timber.d("no selected image")
        } else {
            gameViewModel.uploadImage(uri)
        }
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
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.snackbar_text_good_move),
                        actionLabel = context.getString(R.string.snackbar_label_text_good_move),
                        duration = SnackbarDuration.Short,
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
        positionsToHighlight.clear()
    }

    val handlePanelClick: (PanelState) -> Unit = {
        shouldShowAnswerButton = false
        if (panelClickedOnce) {
            panelClickedOnce = false
            positionStack.add(Position(it.row, it.column))
            var move = Move(positionStack.first(), positionStack.last())
            if (gameViewModel.mustPromote(move)) {
                move = move.copy(isPromote = true)
            }
            // 指し手の確定タイミングは成の余地の有無でDialog前後に分岐する
            if (gameViewModel.listLegalMoves(lastClickedPanel)
                .contains(positionStack.last()) &&
                gameViewModel.isPromotable(move) &&
                !move.isPromote
            ) {
                // decide to promote in dialog
                shouldShowPromotionDialog = true
            } else {
                if (isRegisterQuestionMode) {
                    registerMove(move)
                } else {
                    processMove(move)
                }
            }
        } else {
            if (it.pieceKind != PieceKind.EMPTY) {
                panelClickedOnce = true
                positionStack.add(Position(it.row, it.column))
                lastClickedPanelPos = Position(it.row, it.column)
                lastClickedPanel = it
                positionsToHighlight.addAll(gameViewModel.listLegalMoves(it))
            }
        }
    }

    // komadai
    val handleKomadaiClick: (PieceKind) -> Unit = {
        if (panelClickedOnce) {
            panelClickedOnce = false
            positionStack.clear()
        } else {
            panelClickedOnce = true
            positionStack.add(Position(MY_KOMADAI_INDEX, it.ordinal)) // move.fromにpiecekindを埋め込んでいる
            positionsToHighlight.clear()
            positionsToHighlight.addAll(
                gameViewModel.listLegalMovesFromKomadai(
                    it,
                    isEnemy = false
                )
            )
            lastClickedPanelPos = Position(-1, -1) // 駒台を表す
        }
    }
    val handleEnemyKomadaiClick: (PieceKind) -> Unit = {
        if (panelClickedOnce) {
            panelClickedOnce = false
        } else {
            panelClickedOnce = true
            positionStack.add(
                Position(
                    ENEMY_KOMADAI_INDEX,
                    it.ordinal
                )
            ) // move.fromにpiecekindを埋め込んでいる
            positionsToHighlight.clear()
            positionsToHighlight.addAll(
                gameViewModel.listLegalMovesFromKomadai(
                    it,
                    isEnemy = true
                )
            )
            lastClickedPanelPos = Position(-1, -1) // 駒台を表す
        }
    }

    val handleShowAnswerClick: () -> Unit = {
        showAnswerMode = true
        shouldShowAnswerButton = false
        scope.launch {
            scaffoldState.bottomSheetState.expand()
        }
        positionsToHighlight.addAll(listOf(question.answerMove.from, question.answerMove.to))
    }
    val handleRestartClick: () -> Unit = {
        shouldShowAnswerButton = true
        // initialize state for highlight
        panelClickedOnce = false
        lastClickedPanelPos = NonPosition
        positionsToHighlight.clear()
        restartQuestion()
        scope.launch {
            scaffoldState.bottomSheetState.hide()
        }
    }

    val piecesCount: Map<PieceKind, Int> = gameViewModel.komadaiState.groupingBy { it }.eachCount()
    val enemyPiecesCount: Map<PieceKind, Int> =
        gameViewModel.enemyKomadaiState.groupingBy { it }.eachCount()

    BottomSheetScaffold(
        sheetContent = {
            Column(
                Modifier.padding(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 8.dp
                )
            ) {
                VisibleIf(condition = !isAnswerDescriptionEditMode) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(onClick = {
                            isAnswerDescriptionEditMode = true
                        }) {
                            Icon(Icons.Default.EditNote, "edit answer description")
                        }
                    }
                }
                if (isAnswerDescriptionEditMode) {
                    TextField(
                        value = answerDescriptionTextInput,
                        onValueChange = { answerDescriptionTextInput = it },
                        label = { Text("解説") },
                        placeholder = { Text("解説を入力してください") },
                        trailingIcon = {
                            IconButton(
                                onClick = { isAnswerDescriptionEditMode = false },
                            ) {
                                Icon(Icons.Default.Save, "save answer description")
                            }
                        }
                    )
                } else {
                    if (question.answerDescription.isEmpty()) {
                        Text(text = "question.answerDescription is empty")
                    } else {
                        Text(question.answerDescription)
                    }
                }
            }
        },
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { snackbarData: SnackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    actionColor = BoardColor,
                )
            }
        },
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
                            onCheckedChange = { shouldShowSFENInput = !shouldShowSFENInput }
                        ) {
                            Icon(Icons.Filled.TextRotateVertical, "toggle decode SFEN input form")
                        }
                    }
                    VisibleIf(BuildConfig.DEBUG && !isRegisterQuestionMode) {
                        IconButton(onClick = {
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }) {
                            Icon(Icons.Default.Screenshot, "access to screenshots")
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
                }
            )

            if (uiState.isRequesting) {
                LinearProgressIndicator(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(Modifier.size(4.dp))
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

            val clipboardManager = LocalClipboardManager.current
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
                                        clipboardManager.setText(
                                            buildAnnotatedString {
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
                                        Icons.Filled.ContentCopy,
                                        null,
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
                                        Icons.Filled.Sync,
                                        "load SFEN",
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
                                gameViewModel.boardState[
                                    moveToRegister.to.row * BOARD_SIZE +
                                        moveToRegister.to.column
                                ].pieceKind
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
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                navigateToPrevQuestion.invoke()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "back to prev question",
                                modifier = Modifier.size(
                                    ButtonDefaults.IconSize
                                )
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text("前の問題へ")
                        }
                        OutlinedButton(
                            onClick = {
                                navigateToNextQuestion.invoke()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                        ) {
                            Text("次の問題へ")
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "back to prev question",
                                modifier = Modifier.size(
                                    ButtonDefaults.IconSize
                                )
                            )
                        }
                    }

                    if (shouldShowAnswerButton) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = handleShowAnswerClick,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                            ) {
                                Text(text = "回答を表示")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            OutlinedButton(
                                onClick = {
                                    handleRestartClick()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                            ) {
                                Text(text = "問題をリセット")
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
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(text = stringResource(R.string.dialog_title_promote))
            },
            confirmButton = {
                Button(onClick = onConfirmClick) {
                    Text(text = stringResource(R.string.button_text_confirm_promotion))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = onDismissClick) {
                    Text(text = stringResource(R.string.button_text_cancel_promotion))
                }
            }
        )
    }
}
