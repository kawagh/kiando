package jp.kawagh.kiando.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.Question
import jp.kawagh.kiando.sampleQuestionWithLongDescription
import jp.kawagh.kiando.ui.theme.CardColor
import jp.kawagh.kiando.ui.theme.KiandoM3Theme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionCard(
    question: Question,
    onClick: () -> Unit,
    handleDeleteAQuestion: () -> Unit,
    handleFavoriteQuestion: () -> Unit,
    showIcons: Boolean = true
) {
    val isFavorite = question.tag_id == 1
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = CardColor
        ),
        modifier = Modifier.padding(start = 4.dp, end = 4.dp)
    ) {
        Column(modifier = Modifier.height(80.dp)) {
            Text(
                text = question.description,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                modifier = Modifier.padding(4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (showIcons) {
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
                                LocalContentColor.current
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun QuestionCardPreview() {
    KiandoM3Theme() {
        QuestionCard(sampleQuestionWithLongDescription, {}, {}, {})
    }
}
