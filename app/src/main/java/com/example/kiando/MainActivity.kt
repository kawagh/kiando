package com.example.kiando

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.kiando.ui.theme.KiandoTheme

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
                        handleDeleteQuestions = { questionsViewModel.deleteAll() },
                        handleDeleteAQuestion = { question ->
                            questionsViewModel.deleteQuestion(
                                question
                            )
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
                    MainScreen(
                        question = question,
                        navigateToList = { navController.navigate("list") },
                        navigateToNextQuestion = { navigateToQuestion(nextQuestion) },
                    )
                }
            }
        }
    }
}