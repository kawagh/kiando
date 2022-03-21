package com.example.kiando

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Preview
@Composable
fun MainScreen() {
    Board()
}

@Composable
fun Board() {
    val b = initialBoardState
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        repeat(9) { rowIndex ->
            BoardRow(rowIndex, b[rowIndex])
        }
    }
}

@Composable
fun BoardRow(rowIndex: Int, row: List<PanelState>) {
    Row() {
        repeat(9) { colIndex ->
            Panel(rowIndex * 9 + colIndex, row[colIndex])

        }
    }
}

@Composable
fun Panel(state: Int, panelState: PanelState) {
    val context = LocalContext.current
    Button(
        onClick = { Toast.makeText(context, "$state clicked", Toast.LENGTH_SHORT).show() },
        modifier = Modifier.size(40.dp)
    ) {
        when (panelState) {
            is PanelState.Empty -> Text(text = "", fontSize = 15.sp)
            is PanelState.Piece -> {
                val text = when (panelState.pieceKind) {
                    PieceKind.KING -> "王"
                    PieceKind.ROOK -> if (panelState.isPromoted) "龍" else "飛"
                    PieceKind.BISHOP -> if (panelState.isPromoted) "馬" else "角"
                    PieceKind.GOLD -> "金"
                    PieceKind.SILVER -> if (panelState.isPromoted) "全" else "銀"
                    PieceKind.KNIGHT -> if (panelState.isPromoted) "圭" else "桂"
                    PieceKind.LANCE -> if (panelState.isPromoted) "杏" else "香"
                    PieceKind.PAWN -> if (panelState.isPromoted) "と" else "歩"
                }
                Text(
                    text = text, fontSize = 15.sp,
                    modifier = if (panelState.isEnemy) Modifier.rotate(180f) else Modifier
                )
            }
        }
    }
}