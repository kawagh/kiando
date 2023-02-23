package jp.kawagh.kiando

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import jp.kawagh.kiando.models.Tag
import jp.kawagh.kiando.ui.theme.KiandoM3Theme
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var viewModelAssistedFactory: GameViewModel.GameViewModelAssistedFactory
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTimber()
        setContent {
            App(gameViewModelAssistedFactory = viewModelAssistedFactory)
        }
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    questionsViewModel: QuestionsViewModel = viewModel(),
    gameViewModelAssistedFactory: GameViewModel.GameViewModelAssistedFactory
) {
    val uiState = questionsViewModel.uiState
    KiandoM3Theme(darkTheme = false) {
        // A surface container using the 'background' color from the theme
        val navController = rememberNavController()
        val navigateToQuestion: (Question, fromTabIndex: Int) -> Unit = { question, fromTabIndex ->
            navController.navigate("main/${question.id}/${fromTabIndex}")
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
                        questionsUiState = uiState,
                        navigateToQuestion = navigateToQuestion,
                        navigateToDelete = { navController.navigate("delete") },
                        navigateToLicense = { navController.navigate("license") },
                        handleDeleteAQuestion = { question ->
                            navController.navigate("delete_each/${question.id}")
                        },
                        handleRenameAQuestion = { question ->
                            navController.navigate("rename/${question.id}")
                        },
                        handleFavoriteQuestion = { question ->
                            questionsViewModel.toggleQuestionFavorite(question)
                        },
                        handleInsertSampleQuestions = {
                            questionsViewModel.addSampleQuestionsAndTags()
                        },
                        handleLoadDataFromResource = {
                            questionsViewModel.loadDataFromAsset()
                        },
                        handleAddTag = { tag: Tag -> questionsViewModel.add(tag) },
                        handleToggleCrossRef = { question: Question, tag: Tag ->
                            questionsViewModel.toggleCrossRef(question, tag)
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
                    val nextQuestion =
                        if (fromTabIndex == favoriteIndex) {
                            uiState.questionsWithTags.filter { q -> q.question.isFavorite }
                                .find { q ->
                                    q.question.id > questionId
                                }?.question
                                ?: sampleQuestion
                        } else {
                            uiState.questionsWithTags.find { q -> q.question.id > questionId }?.question
                                ?: sampleQuestion
                        }
                    val prevQuestion =
                        if (fromTabIndex == favoriteIndex) {
                            uiState.questionsWithTags.filter { q -> q.question.isFavorite }
                                .findLast { q ->
                                    q.question.id < questionId
                                }?.question
                                ?: sampleQuestion
                        } else {
                            uiState.questionsWithTags.findLast { q -> q.question.id < questionId }?.question
                                ?: sampleQuestion
                        }
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
                        title = { Text(text = "delete all questions?") },
                        text = { Text(text = "Once you delete questions, you cannot recover them.") },
                        confirmButton = {
                            TextButton(onClick = {
                                questionsViewModel.deleteAll()
                                navController.navigate("list")
                            }) {
                                Text(text = "DELETE")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { navController.navigate("list") }) {
                                Text(text = "CANCEL")
                            }
                        }
                    )
                }
                dialog("delete_each/{questionId}",
                    arguments = listOf(navArgument("questionId") {
                        type = NavType.IntType
                    }
                    )) {
                    val deleteId = it.arguments?.getInt("questionId") ?: -1
                    AlertDialog(
                        onDismissRequest = { navController.navigate("list") },
                        title = { Text(text = "delete question?") },
                        text = { Text(text = "Once you delete a question, you cannot recover it.") },
                        confirmButton = {
                            TextButton(onClick = {
                                navController.navigate("list")
                                questionsViewModel.deleteById(deleteId)
                            }) {
                                Text(text = "DELETE")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { navController.navigate("list") }) {
                                Text(text = "CANCEL")
                            }
                        }
                    )
                }

                dialog(
                    "rename/{questionId}",
                    arguments = listOf(navArgument("questionId") {
                        type = NavType.IntType
                    })
                ) {
                    var renameTextInput by remember {
                        mutableStateOf("")
                    }
                    val renameId = it.arguments?.getInt("questionId") ?: -1
                    val focusRequester = remember { FocusRequester() }
                    AlertDialog(onDismissRequest = { navController.navigate("list") },
                        title = { Text(text = "問題の名前の変更") },
                        text = {
                            OutlinedTextField(
                                value = renameTextInput,
                                label = { Text("新しい名前") },
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
                                    questionsViewModel.renameById(
                                        questionId = renameId,
                                        newTitle = renameTextInput
                                    )
                                    navController.navigate("list")
                                },
                                enabled = renameTextInput.isNotEmpty()
                            ) {
                                Text(text = "変更")
                            }
                        }
                    )
                }
            }
        }
    }
}