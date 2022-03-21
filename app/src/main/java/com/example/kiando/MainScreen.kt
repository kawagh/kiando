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
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        repeat(9) { rowIndex ->
            BoardRow(rowIndex)
        }
    }
}

@Composable
fun BoardRow(rowIndex: Int) {
    Row() {
        repeat(9) { colIndex ->
            Panel(rowIndex * 9 + colIndex)

        }
    }
}

@Composable
fun Panel(state: Int) {
    val context = LocalContext.current
    Button(
        onClick = { Toast.makeText(context, "$state clicked", Toast.LENGTH_SHORT).show() },
        modifier = Modifier.size(40.dp)
    ) {
        Text(text = state.toString(), fontSize = 10.sp)
    }
}