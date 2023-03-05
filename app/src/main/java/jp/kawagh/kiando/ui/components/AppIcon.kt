package jp.kawagh.kiando.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun AppIcon(withDebug: Boolean = true) {
    val offsets = listOf(
        Pair(0.dp, 10.dp),
        Pair(15.dp, 0.dp),
        Pair(0.dp, 30.dp),
        Pair(15.dp, 20.dp),
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(123.dp)
                .border(width = 1.dp, Color.Black)
                .rotate(20f),
        ) {
            Box(modifier = Modifier.offset(x = 30.dp, y = 15.dp)) {
                if (withDebug) {
                    Text("DEBUG", modifier = Modifier.offset(x = (-10).dp, y = 5.dp))
                }
                repeat(offsets.size) {
                    Piece(
                        text = " ",
                        onClick = {},
                        modifier = Modifier
                            .offset(x = offsets[it].first, y = offsets[it].second)
                            .size(40.dp),
                        pieceColor = Color.Black
                    )
                }
            }
        }
    }
}