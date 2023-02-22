package jp.kawagh.kiando.ui.components

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import jp.kawagh.kiando.models.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagChip(
    tag: Tag,
    onClick: () -> Unit,
    containerColor: Color = Color.Transparent,
) {
    AssistChip(
        onClick = onClick, label = { Text(tag.title) },
        interactionSource = NoRippleInteractionSource(),
        colors = AssistChipDefaults.assistChipColors(containerColor = containerColor),
    )
}

@Preview
@Composable
private fun TagChipPreview() {
    TagChip(tag = Tag(title = "序盤"), {})
}

private class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction) = true
}