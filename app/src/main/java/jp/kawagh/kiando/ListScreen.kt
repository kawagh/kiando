package jp.kawagh.kiando

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.ui.theme.BoardColor

@Preview
@Composable
fun PreviewListScreen() {
    ListScreen(sampleQuestions, {}, {}, {})

}

@Composable
fun ListScreen(
    questions: List<Question>,
    navigateToQuestion: (Question) -> Unit,
    handleDeleteQuestions: () -> Unit,
    handleDeleteAQuestion: (Question) -> Unit,
) {
    var showDeleteDialog by remember {
        mutableStateOf(false)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, null)
                    }
                }
            )
        },
        content = {
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { /*TODO*/ },
                    title = { Text(text = "delete all questions?") },
                    text = { Text(text = "Once you delete questions, you cannot recover them.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            handleDeleteQuestions.invoke()
                        }) {
                            Text(text = "DELETE")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(text = "CANCEL")
                        }
                    }
                )
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = "Problem Set", fontSize = MaterialTheme.typography.h4.fontSize)
                QuestionsList(
                    questions = questions,
                    navigateToQuestion = navigateToQuestion,
                    handleDeleteAQuestion = handleDeleteAQuestion,
                )
            }
        },
    )
}

@Composable
fun QuestionsList(
    questions: List<Question>,
    navigateToQuestion: (Question) -> Unit,
    handleDeleteAQuestion: (Question) -> Unit,
) {
    LazyColumn {
        items(questions) { question ->
            QuestionRow(
                question = question,
                onClick = { navigateToQuestion(question) },
                handleDeleteAQuestion = { handleDeleteAQuestion(question) },
            )
            Spacer(Modifier.size(5.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestionRow(question: Question, onClick: () -> Unit, handleDeleteAQuestion: () -> Unit) {
    var showDeleteButton by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .width(300.dp)
            .clip(shape = RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteButton = !showDeleteButton },
            )
            .background(BoardColor),
        horizontalArrangement = Arrangement.Center,
    )
    {
        Text(text = question.description, fontSize = MaterialTheme.typography.h5.fontSize)
        if (showDeleteButton) {
            Button(onClick = {
                handleDeleteAQuestion.invoke()
                showDeleteButton = false
            }) {
                Text(text = "delete")
            }
        }
    }
}

@Composable
fun StableQuestionRow(question: Question, onClick: () -> Unit, handleDeleteAQuestion: () -> Unit) {
    Row(
        modifier = Modifier
            .width(300.dp)
            .clip(shape = RoundedCornerShape(20.dp))
            .clickable { onClick.invoke() }
            .background(BoardColor),
        horizontalArrangement = Arrangement.Center,
    )
    {
        Text(text = question.description, fontSize = MaterialTheme.typography.h5.fontSize)
    }
}