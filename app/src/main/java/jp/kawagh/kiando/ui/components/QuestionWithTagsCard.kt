package jp.kawagh.kiando.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jp.kawagh.kiando.QuestionWithTags
import jp.kawagh.kiando.models.Tag
import jp.kawagh.kiando.sampleQuestion
import jp.kawagh.kiando.ui.theme.CardColor
import jp.kawagh.kiando.ui.theme.KiandoM3Theme


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestionWithTagsCard(
    questionWithTags: QuestionWithTags,
    onClick: () -> Unit,
    handleDeleteAQuestion: () -> Unit,
    handleRenameAQuestion: () -> Unit,
    handleFavoriteQuestion: () -> Unit,
    showIcons: Boolean = true
) {
    val question = questionWithTags.question
    val tags = questionWithTags.tags
    Card(
        colors = CardDefaults.cardColors(containerColor = CardColor),
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp)
            .combinedClickable(onLongClick = handleRenameAQuestion, onClick = onClick)
    ) {
        Column(modifier = Modifier.height(80.dp)) {
            Text(
                text = question.description,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                modifier = Modifier.padding(4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row {
                Row() {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.Start),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        items(tags) {
                            TagChip(tag = it, {})
                        }
                    }
                }
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
                                tint = if (question.isFavorite) {
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
}

@Preview
@Composable
fun QuestionWithTagsPreview() {
    val questionWithTags =
        QuestionWithTags(sampleQuestion, listOf(Tag(title = "sample"), Tag(title = "序盤")))
    KiandoM3Theme() {
        QuestionWithTagsCard(questionWithTags, {}, {}, {}, {})
    }
}