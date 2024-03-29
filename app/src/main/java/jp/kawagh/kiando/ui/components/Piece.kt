package jp.kawagh.kiando.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.kawagh.kiando.ui.theme.PieceColor

@Composable
fun Piece(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnemy: Boolean = false,
    isPromoted: Boolean = false,
    pieceColor: Color = PieceColor,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clickable { onClick.invoke() }
            .rotate((if (isEnemy) 180f else 0f))
    ) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            if (text.isNotEmpty()) {
                drawPath(
                    path = Path().apply {
                        moveTo(w / 2, h / 6)
                        lineTo(3 * w / 4, h / 3)
                        lineTo(5 * w / 6, 3 * h / 4)
                        lineTo(w / 6, 3 * h / 4)
                        lineTo(w / 4, h / 3)
                        close()
                    },
                    color = pieceColor,
                )
            }
        }
        Text(text = text, fontSize = 17.sp, color = if (isPromoted) Color.Red else Color.Black)
    }
}

@Preview
@Composable
private fun PiecePreview() {
    Piece(text = "歩", {}, modifier = Modifier.size(40.dp))
}
