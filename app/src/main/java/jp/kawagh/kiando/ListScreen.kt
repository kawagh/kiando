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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
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
    ListScreen(sampleQuestions, {}, {}, {}, {})

}

sealed class TabItem(val name: String) {
    object All : TabItem("All")
    object Tagged : TabItem("Tagged")
}

@Composable
fun ListScreen(
    questions: List<Question>,
    navigateToQuestion: (Question) -> Unit,
    navigateToDelete: () -> Unit,
    handleDeleteAQuestion: (Question) -> Unit,
    handleFavoriteQuestion: (Question) -> Unit,
) {
    var tabRowIndex by remember {
        mutableStateOf(0)
    }
    val tabs = listOf(TabItem.All, TabItem.Tagged)
    val questionsToDisplay = when (tabs[tabRowIndex]) {
        is TabItem.All -> questions
        is TabItem.Tagged -> questions.filter { it.tag_id != null }
    }
    var dropDownExpanded by remember {
        mutableStateOf(false)
    }
    val dropDownMenuItems = mapOf(
        "Delete Questions" to navigateToDelete,
        "Version: ${BuildConfig.VERSION_NAME}" to {}
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },

                actions = {
                    IconButton(onClick = { dropDownExpanded = !dropDownExpanded }) {
                        Icon(Icons.Default.MoreVert, null)
                    }
                    DropdownMenuOnTopBar(
                        dropDownMenuItems,
                        expanded = dropDownExpanded,
                        setExpanded = { dropDownExpanded = it })
                }
            )
        },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TabRow(selectedTabIndex = tabRowIndex) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(selected = tabRowIndex == index, onClick = { tabRowIndex = index }) {
                            Text(tab.name, fontSize = MaterialTheme.typography.h5.fontSize)
                        }
                    }
                }
                Text(text = "Problem Set", fontSize = MaterialTheme.typography.h4.fontSize)
                QuestionsList(
                    questions = questionsToDisplay,
                    navigateToQuestion = navigateToQuestion,
                    handleDeleteAQuestion = handleDeleteAQuestion,
                    handleFavoriteQuestion = handleFavoriteQuestion,
                )
            }
        },
    )
}

@Composable
fun DropdownMenuOnTopBar(
    dropDownMenuItems: Map<String, () -> Unit>,
    expanded: Boolean,
    setExpanded: (Boolean) -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = { setExpanded(false) }) {
        dropDownMenuItems.forEach { (name, callback) ->
            DropdownMenuItem(onClick = {
                callback.invoke()
                setExpanded(false)
            }) {
                Text(text = name)
            }
        }
    }
}


@Composable
fun QuestionsList(
    questions: List<Question>,
    navigateToQuestion: (Question) -> Unit,
    handleDeleteAQuestion: (Question) -> Unit,
    handleFavoriteQuestion: (Question) -> Unit
) {
    LazyColumn {
        items(questions) { question ->
            QuestionRow(
                question = question,
                onClick = { navigateToQuestion(question) },
                handleDeleteAQuestion = { handleDeleteAQuestion(question) },
                handleFavoriteQuestion = { handleFavoriteQuestion(question) },
            )
            Spacer(Modifier.size(5.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestionRow(
    question: Question, onClick: () -> Unit,
    handleDeleteAQuestion: () -> Unit,
    handleFavoriteQuestion: () -> Unit,
) {
    var showButtons by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showButtons = !showButtons },
            )
            .background(BoardColor),
        horizontalArrangement = if (showButtons) Arrangement.Start else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        if (showButtons) {
            IconButton(
                onClick = { handleFavoriteQuestion.invoke() },
            ) {
                Icon(
                    if (question.tag_id == 1) Icons.Default.Star else Icons.Default.Remove,
                    "toggle favorite",
                )
            }
            IconButton(onClick = {
                handleDeleteAQuestion.invoke()
                showButtons = false
            }) {
                Icon(Icons.Default.Delete, "delete")
            }
        }
        Text(text = question.description, fontSize = MaterialTheme.typography.h5.fontSize)
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
