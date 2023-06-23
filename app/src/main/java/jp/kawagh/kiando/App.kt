package jp.kawagh.kiando

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import jp.kawagh.kiando.models.Question
import jp.kawagh.kiando.models.Tag
import jp.kawagh.kiando.models.sampleQuestion
import jp.kawagh.kiando.ui.screens.EntryScreen
import jp.kawagh.kiando.ui.screens.LicenseScreen
import jp.kawagh.kiando.ui.screens.ListScreen
import jp.kawagh.kiando.ui.screens.MainScreen
import jp.kawagh.kiando.ui.screens.TabItem
import jp.kawagh.kiando.ui.theme.KiandoM3Theme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    questionsViewModel: QuestionsViewModel = viewModel(),
    gameViewModelAssistedFactory: GameViewModel.GameViewModelAssistedFactory,
) {
    val uiState = questionsViewModel.uiState
    val appliedFilterName = questionsViewModel.appliedFilterName.collectAsState(initial = "").value
    KiandoM3Theme(darkTheme = false) {
        // A surface container using the 'background' color from the theme
        val navController = rememberNavController()
        val navigateToQuestion: (Question, fromTabIndex: Int) -> Unit = { question, fromTabIndex ->
            navController.navigate("main/${question.id}/$fromTabIndex")
        }
        val navigateToList: () -> Unit = {
            navController.navigate("list")
        }

        SideEffectChangeSystemUi()
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(navController = navController, startDestination = "list") {
                composable("entry") {
                    EntryScreen(onNavigateList = { navController.navigate("list") })
                }

                composable("list") {
                    ListScreen(
                        questionsViewModel = questionsViewModel,
                        navigateToQuestion = navigateToQuestion,
                        navigateToDelete = { navController.navigate("delete") },
                        navigateToLicense = { navController.navigate("license") },
                        handleDeleteAQuestion = { question ->
                            navController.navigate("delete_each/${question.id}")
                        },
                        handleRenameQuestion = { question ->
                            navController.navigate("rename/${question.id}")
                        },
                        handleRenameTag = { tag ->
                            navController.navigate("rename_tag/${tag.id}")
                        },
                    )
                }

                composable(
                    "main/{questionId}/{fromTabIndex}",
                    arguments = listOf(
                        navArgument("questionId") {
                            type = NavType.IntType
                        },
                        navArgument("fromTabIndex") { type = NavType.IntType },
                    )
                ) {
                    val questionId = it.arguments?.getInt("questionId") ?: -1

                    // used to decide next question
                    val fromTabIndex = it.arguments?.getInt("fromTabIndex") ?: 0

                    val question =
                        uiState.questionsWithTags.find { question -> question.question.id == questionId }?.question
                            ?: sampleQuestion

                    val favoriteIndex = TabItem.values().indexOf(TabItem.Favorite)
                    val containAppliedFilter: (List<Tag>) -> Boolean = { tags ->
                        appliedFilterName.isEmpty() ||
                                tags.map { tag -> tag.title }.contains(appliedFilterName)
                    }
                    val questionsWithTags = uiState.questionsWithTags.filter { qts ->
                        (fromTabIndex != favoriteIndex || qts.question.isFavorite) &&
                                containAppliedFilter(qts.tags)
                    }
                    val nextQuestion = questionsWithTags
                        .find { q ->
                            q.question.id > questionId
                        }?.question
                        ?: sampleQuestion
                    val prevQuestion = questionsWithTags
                        .findLast { qts ->
                            qts.question.id < questionId
                        }?.question
                        ?: sampleQuestion
                    val gameViewModel: GameViewModel = gameViewModelAssistedFactory.create(question)
                    MainScreen(
                        gameViewModel = gameViewModel,
                        question = question,
                        navigateToList = navigateToList,
                        navigateToNextQuestion = {
                            navigateToQuestion(
                                nextQuestion,
                                fromTabIndex
                            )
                        },
                        navigateToPrevQuestion = {
                            navigateToQuestion(
                                prevQuestion,
                                fromTabIndex
                            )
                        },
                    )
                }

                composable("license") {
                    LicenseScreen(onArrowBackPressed = navigateToList)
                }

                dialog("delete") {
                    AlertDialog(
                        onDismissRequest = { navController.navigate("list") },
                        title = { Text(text = stringResource(R.string.dialog_title_delete_all_questions)) },
                        text = { Text(text = stringResource(R.string.dialog_text_delete_all_questions)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    questionsViewModel.deleteAllQuestions()
                                    navController.navigate("list")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                )
                            ) {
                                Text(text = stringResource(R.string.dialog_text_confirm_delete))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { navController.navigate("list") }) {
                                Text(text = stringResource(R.string.dialog_text_cancel))
                            }
                        }
                    )
                }

                dialog(
                    "delete_each/{questionId}",
                    arguments = listOf(
                        navArgument("questionId") {
                            type = NavType.IntType
                        }
                    )
                ) {
                    val deleteId = it.arguments?.getInt("questionId") ?: -1
                    AlertDialog(
                        onDismissRequest = { navController.navigate("list") },
                        title = { Text(text = stringResource(R.string.dialog_title_delete_question)) },
                        text = { Text(text = stringResource(R.string.dialog_text_delete_question)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    navController.navigate("list")
                                    questionsViewModel.deleteQuestionById(deleteId)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                )
                            ) {
                                Text(text = stringResource(R.string.dialog_text_confirm_delete))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { navController.navigate("list") }) {
                                Text(text = stringResource(R.string.dialog_text_cancel))
                            }
                        }
                    )
                }

                dialog(
                    "rename/{questionId}",
                    arguments = listOf(
                        navArgument("questionId") {
                            type = NavType.IntType
                        }
                    )
                ) {
                    var renameTextInput by remember {
                        mutableStateOf("")
                    }
                    val renameId = it.arguments?.getInt("questionId") ?: -1
                    val focusRequester = remember { FocusRequester() }
                    AlertDialog(
                        onDismissRequest = { navController.navigate("list") },
                        title = { Text(text = stringResource(R.string.dialog_title_rename_question)) },
                        text = {
                            OutlinedTextField(
                                value = renameTextInput,
                                label = { Text(stringResource(R.string.label_text_new_name)) },
                                onValueChange = { renameTextInput = it },
                                modifier = Modifier.focusRequester(focusRequester)
                            )
                            LaunchedEffect(Unit) {
                                delay(100) // workaround to show keyboard
                                // ref: https://issuetracker.google.com/issues/204502668
                                focusRequester.requestFocus()
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    questionsViewModel.renameQuestionById(
                                        questionId = renameId,
                                        newTitle = renameTextInput
                                    )
                                    navController.navigate("list")
                                },
                                enabled = renameTextInput.isNotEmpty()
                            ) {
                                Text(text = stringResource(R.string.button_text_confirm_change))
                            }
                        }
                    )
                }

                dialog(
                    "rename_tag/{tagId}",
                    arguments = listOf(
                        navArgument("tagId") {
                            type = NavType.IntType
                        }
                    )
                ) {
                    var renameTextInput by remember {
                        mutableStateOf("")
                    }
                    val renameId = it.arguments?.getInt("tagId") ?: -1
                    val focusRequester = remember { FocusRequester() }
                    AlertDialog(
                        onDismissRequest = { navController.navigate("list") },
                        title = { Text(text = stringResource(R.string.dialog_title_rename_tag)) },
                        text = {
                            OutlinedTextField(
                                value = renameTextInput,
                                label = { Text(stringResource(id = R.string.label_text_new_name)) },
                                onValueChange = { renameTextInput = it },
                                modifier = Modifier.focusRequester(focusRequester)
                            )
                            LaunchedEffect(Unit) {
                                delay(100) // workaround to show keyboard
                                // ref: https://issuetracker.google.com/issues/204502668
                                focusRequester.requestFocus()
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    questionsViewModel.renameTagId(
                                        tagId = renameId,
                                        newTitle = renameTextInput
                                    )
                                    navController.navigate("list")
                                },
                                enabled = renameTextInput.isNotEmpty()
                            ) {
                                Text(text = stringResource(R.string.button_text_confirm_change))
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SideEffectChangeSystemUi() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
}
