package com.example.kiando

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun PreviewListScreen() {
    ListScreen { }

}

@Composable
fun ListScreen(onNavigateMain: () -> Unit) {
    val questions = sampleQuestions
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "List Screen")
        Button(onClick = onNavigateMain) {
            Text(text = "List")
        }
        QuestionsList(questions = questions)

    }
}

@Composable
fun QuestionsList(questions: List<Question>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        items(questions) { question ->
            QuestionRow(question)
        }
    }
}

@Composable
fun QuestionRow(question: Question) {
    Row() {
        Text(text = question.description)
        Button(onClick = { /*TODO*/ }) {
            Text(text = "solve")
        }
    }
}

