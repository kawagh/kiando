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
        val navigateToQuestion: (Int) -> Unit = { questionId ->
            navController.navigate("main/${questionId}")
        }
        val userAddedQuestions by questionsViewModel.questions.observeAsState(initial = listOf())
        val allQuestions = sampleQuestions + userAddedQuestions
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            NavHost(navController = navController, startDestination = "entry") {
                composable("entry") {
                    EntryScreen(onNavigateList = { navController.navigate("list") })
                }
                composable("list") {
                    ListScreen(
                        questions = allQuestions,
                        onNavigateMain = { navController.navigate("main/0") },
                        navigateToQuestion,
                        { questionsViewModel.deleteAll() }
                    )
                }
                composable(
                    "main/{questionId}",
                    arguments = listOf(navArgument("questionId") {
                        type = NavType.IntType
                    })
                ) {
                    val questionId = it.arguments?.getInt("questionId") ?: 0
                    val question =
                        allQuestions.find { question -> question.id == questionId }
                            ?: sampleQuestion
                    MainScreen(question = question)
                }
            }
        }
    }
}