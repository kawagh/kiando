package jp.kawagh.kiando

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import jp.kawagh.kiando.ui.theme.KiandoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            App()
        }
    }
}

@Composable
fun App(questionsViewModel: QuestionsViewModel = viewModel()) {
    KiandoTheme {
        // A surface container using the 'background' color from the theme
        val navController = rememberNavController()
        val navigateToQuestion: (Question) -> Unit = { it ->
            navController.navigate("main/${it.id}")
        }
        val userAddedQuestions by questionsViewModel.questions.observeAsState(initial = listOf())
        val allQuestions = sampleQuestions + userAddedQuestions

        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        SideEffect {
            systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = useDarkIcons
            )
        }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            NavHost(navController = navController, startDestination = "list") {
                composable("entry") {
                    EntryScreen(onNavigateList = { navController.navigate("list") })
                }
                composable("list") {
                    ListScreen(
                        questions = allQuestions,
                        navigateToQuestion = navigateToQuestion,
                        navigateToDelete = { navController.navigate("delete") },
                        handleDeleteAQuestion = { question ->
                            navController.navigate("delete_each/${question.id}")
                        },
                        handleFavoriteQuestion = { question ->
                            questionsViewModel.toggleQuestionFavorite(question)
                        }
                    )
                }
                composable(
                    "main/{questionId}",
                    arguments = listOf(navArgument("questionId") {
                        type = NavType.IntType
                    })
                ) {
                    val questionId = it.arguments?.getInt("questionId") ?: -1
                    val question =
                        allQuestions.find { question -> question.id == questionId }
                            ?: sampleQuestion
                    val nextQuestion =
                        allQuestions.find { question -> question.id > questionId } ?: sampleQuestion
                    val prevQuestion =
                        allQuestions.find { question -> question.id < questionId } ?: sampleQuestion
                    MainScreen(
                        question = question,
                        navigateToList = { navController.navigate("list") },
                        navigateToNextQuestion = { navigateToQuestion(nextQuestion) },
                        navigateToPrevtQuestion = { navigateToQuestion(prevQuestion) },
                    )
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
            }
        }
    }
}