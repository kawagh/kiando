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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import jp.kawagh.kiando.ui.theme.KiandoM3Theme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTimber()
        setContent {
            App()
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
fun App(questionsViewModel: QuestionsViewModel = viewModel()) {
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
                        questions = uiState.questions,
                        navigateToQuestion = navigateToQuestion,
                        navigateToDelete = { navController.navigate("delete") },
                        navigateToLicense = { navController.navigate("license") },
                        handleDeleteAQuestion = { question ->
                            navController.navigate("delete_each/${question.id}")
                        },
                        handleRenameAQuestion = { question ->
                            if (question.id >= 0) {
                                navController.navigate("rename/${question.id}")
                            }
                        },
                        handleFavoriteQuestion = { question ->
                            questionsViewModel.toggleQuestionFavorite(question)
                        },
                        handleInsertSampleQuestions = {
                            questionsViewModel.addSampleQuestions()
                        },
                        handleLoadQuestionFromResource = {
                            questionsViewModel.loadQuestionsFromAsset()
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
                    val fromTabIndex = it.arguments?.getInt("fromTabIndex") ?: 0
                    val question =
                        uiState.questions.find { question -> question.id == questionId }
                            ?: sampleQuestion

                    val TAGGED_INDEX = 1 // TabItem.Tagged
                    val nextQuestion =
                        if (fromTabIndex == TAGGED_INDEX) {
                            uiState.questions.filter { q -> q.tag_id == TAGGED_INDEX }.find { q ->
                                q.id > questionId
                            }
                                ?: sampleQuestion
                        } else {
                            uiState.questions.find { q -> q.id > questionId }
                                ?: sampleQuestion
                        }
                    val prevQuestion =
                        if (fromTabIndex == TAGGED_INDEX) {
                            uiState.questions.filter { q -> q.tag_id == TAGGED_INDEX }
                                .findLast { q ->
                                    q.id < questionId
                                }
                                ?: sampleQuestion
                        } else {
                            uiState.questions.findLast { q -> q.id < questionId }
                                ?: sampleQuestion
                        }
                    MainScreen(
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
                    AlertDialog(onDismissRequest = { navController.navigate("list") },
                        title = { Text(text = "rename question") },
                        text = {
                            TextField(
                                value = renameTextInput,
                                onValueChange = { renameTextInput = it })
                        },
                        confirmButton = {
                            Button(onClick = {
                                questionsViewModel.renameById(
                                    questionId = renameId,
                                    newTitle = renameTextInput
                                )
                                navController.navigate("list")
                            }) {
                                Text(text = "OK")
                            }
                        }
                    )
                }
            }
        }
    }
}