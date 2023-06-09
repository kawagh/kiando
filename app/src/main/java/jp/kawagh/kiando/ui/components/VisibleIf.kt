package jp.kawagh.kiando.ui.components

import androidx.compose.runtime.Composable

@Composable
fun VisibleIf(
    condition: Boolean,
    content: @Composable () -> Unit,
) {
    if (condition) {
        content()
    }
}
