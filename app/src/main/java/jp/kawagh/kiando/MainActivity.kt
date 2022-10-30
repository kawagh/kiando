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
import jp.kawagh.kiando.ui.theme.KiandoM3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun SideEffectChangeSystemUi() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = MaterialTheme.colors.isLight
    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
}

@Composable
fun App(questionsViewModel: QuestionsViewModel = viewModel()) {
    KiandoM3Theme() {
        // A surface container using the 'background' color from the theme
        val navController = rememberNavController()
        val navigateToQuestion: (Question, fromTabIndex: Int) -> Unit = { question, fromTabIndex ->
            navController.navigate("main/${question.id}/${fromTabIndex}")
        }
        val navigateToList: () -> Unit = {
            navController.navigate("list")
        }
        val userAddedQuestions by questionsViewModel.questions.observeAsState(initial = listOf())
        val allQuestions = sampleQuestions + userAddedQuestions

        SideEffectChangeSystemUi()
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
                        navigateToLicense = { navController.navigate("license") },
                        handleDeleteAQuestion = { question ->
                            navController.navigate("delete_each/${question.id}")
                        },
                        handleFavoriteQuestion = { question ->
                            questionsViewModel.toggleQuestionFavorite(question)
                        }
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
                        allQuestions.find { question -> question.id == questionId }
                            ?: sampleQuestion

                    val TAGGED_INDEX = 1 // TabItem.Tagged
                    val nextQuestion =
                        if (fromTabIndex == TAGGED_INDEX) {
                            allQuestions.filter { q -> q.tag_id == TAGGED_INDEX }.find { q ->
                                q.id > questionId
                            }
                                ?: sampleQuestion
                        } else {
                            allQuestions.find { q -> q.id > questionId }
                                ?: sampleQuestion
                        }
                    val prevQuestion =
                        if (fromTabIndex == TAGGED_INDEX) {
                            allQuestions.filter { q -> q.tag_id == TAGGED_INDEX }.findLast { q ->
                                q.id < questionId
                            }
                                ?: sampleQuestion
                        } else {
                            allQuestions.findLast { q -> q.id < questionId }
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
            }
        }
    }
}