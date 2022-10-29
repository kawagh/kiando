package jp.kawagh.kiando.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.Question
import jp.kawagh.kiando.sampleQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionCard(
    question: Question,
    onClick: () -> Unit,
    handleDeleteAQuestion: () -> Unit,
    handleFavoriteQuestion: () -> Unit,
) {
    val isFavorite = question.tag_id == 1
    Card(onClick = onClick) {
        Column() {
            Text(
                text = question.description,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                modifier = Modifier.padding(4.dp)
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = handleDeleteAQuestion) {
                    Icon(Icons.Default.Delete, contentDescription = "delete question")
                }
                IconButton(onClick = {
                    handleFavoriteQuestion.invoke()
                }) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "toggle favorite",
                        tint = if (isFavorite) {
                            Color.Yellow
                        } else {
                            Color.Black
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun QuestionCardPreview() {
    QuestionCard(sampleQuestion, {}, {}, {})
}
