package com.example.kiando

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun BoardRow(rowIndex: Int, row: List<Piece>) {
    Row() {
        repeat(9) { colIndex ->
            Panel(rowIndex * 9 + colIndex, row[colIndex])

        }
    }
}

@Composable
fun Panel(state: Int, piece: Piece) {
    val context = LocalContext.current
    Button(
        onClick = { Toast.makeText(context, "$state clicked", Toast.LENGTH_SHORT).show() },
        modifier = Modifier.size(40.dp)
    ) {
        val text = when (piece) {
            Piece.KING -> "王"
            Piece.ROOK -> "飛"
            Piece.BISHOP -> "角"
            Piece.GOLD -> "金"
            Piece.SILVER -> "銀"
            Piece.KNIGHT -> "桂"
            Piece.LANCE -> "香"
            Piece.PAWN -> "歩"
            Piece.EMPTY -> ""
        }
        Text(text = text, fontSize = 15.sp)
    }
}